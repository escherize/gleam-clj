(ns permissions
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(declare UserId? UserId-schema Empty? NotANumber? OutOfRange? IdError? IdError-schema Viewer? Editor? Owner? Role? Role-schema CanEdit? CanEdit-schema Denied? Denied-schema DashboardId? DashboardId-schema CanEditDashboard? CanEditDashboard-schema)

;; type UserId
(defprotocol IUserId)
(defrecord UserId [value] IUserId)
(defn UserId? "True if `v` is a UserId value." [v] (instance? UserId v))
(defn UserId-schema
  "Malli schema for UserId."
  []
  [:and [:fn UserId?] [:map [:value :int]]])

;; type IdError
(defprotocol IIdError)
(defrecord Empty [] IIdError)
(defn Empty? "True if `v` is a Empty value." [v] (instance? Empty v))
(defrecord NotANumber [^java.lang.String value] IIdError)
(defn NotANumber? "True if `v` is a NotANumber value." [v] (instance? NotANumber v))
(defrecord OutOfRange [value] IIdError)
(defn OutOfRange? "True if `v` is a OutOfRange value." [v] (instance? OutOfRange v))
(defn IdError? "True if `v` is any IdError value." [v] (instance? permissions.IIdError v))
(defn IdError-schema
  "Malli schema for IdError."
  []
  [:or
   [:fn Empty?]
   [:and [:fn NotANumber?] [:map [:value :string]]]
   [:and [:fn OutOfRange?] [:map [:value :int]]]])

;; type Role
(defprotocol IRole)
(defrecord Viewer [] IRole)
(defn Viewer? "True if `v` is a Viewer value." [v] (instance? Viewer v))
(defrecord Editor [] IRole)
(defn Editor? "True if `v` is a Editor value." [v] (instance? Editor v))
(defrecord Owner [] IRole)
(defn Owner? "True if `v` is a Owner value." [v] (instance? Owner v))
(defn Role? "True if `v` is any Role value." [v] (instance? permissions.IRole v))
(defn Role-schema
  "Malli schema for Role."
  []
  [:or
   [:fn Viewer?]
   [:fn Editor?]
   [:fn Owner?]])

;; type CanEdit
(defprotocol ICanEdit)
(defrecord CanEdit [value] ICanEdit)
(defn CanEdit? "True if `v` is a CanEdit value." [v] (instance? CanEdit v))
(defn CanEdit-schema
  "Malli schema for CanEdit."
  []
  [:and [:fn CanEdit?] [:map [:value (UserId-schema)]]])

;; type Denied
(defprotocol IDenied)
(defrecord Denied [who needs] IDenied)
(defn Denied? "True if `v` is a Denied value." [v] (instance? Denied v))
(defn Denied-schema
  "Malli schema for Denied."
  []
  [:and [:fn Denied?] [:map [:who (UserId-schema)] [:needs (Role-schema)]]])

;; type DashboardId
(defprotocol IDashboardId)
(defrecord DashboardId [value] IDashboardId)
(defn DashboardId? "True if `v` is a DashboardId value." [v] (instance? DashboardId v))
(defn DashboardId-schema
  "Malli schema for DashboardId."
  []
  [:and [:fn DashboardId?] [:map [:value :int]]])

;; type CanEditDashboard
(defprotocol ICanEditDashboard)
(defrecord CanEditDashboard [user dash] ICanEditDashboard)
(defn CanEditDashboard? "True if `v` is a CanEditDashboard value." [v] (instance? CanEditDashboard v))
(defn CanEditDashboard-schema
  "Malli schema for CanEditDashboard."
  []
  [:and [:fn CanEditDashboard?] [:map [:user (UserId-schema)] [:dash (DashboardId-schema)]]])

(defn parse-user-id
  "parse_user_id(raw: String) -> Result(UserId, IdError)

   Parse untrusted input into a UserId — the only way to make one."
  {:malli/schema [:=> [:cat :string]
                      (p/result-of (UserId-schema) (IdError-schema))]
   :gleam/src "permissions.gleam:16"}
  [^java.lang.String raw]
  (let [subject (string/trim raw)]
    (if (= subject "")
      (p/->Error (->Empty))
      (let [trimmed subject subject (int/parse trimmed)]
        (cond
          (instance? gleam.prelude.Error subject)
          (p/->Error (->NotANumber trimmed))

          (and (instance? Ok subject) (< (:value subject) 1))
          (let [n (:value subject)]
            (p/->Error (->OutOfRange n)))

          (instance? Ok subject)
          (let [n (:value subject)]
            (p/->Ok (->UserId n))))))))

(defn require-editor
  "require_editor(user: UserId, role: Role) -> Result(CanEdit, Denied)

   Check once, at the boundary. Success mints the proof."
  {:malli/schema [:=> [:cat (UserId-schema) (Role-schema)]
                      (p/result-of (CanEdit-schema) (Denied-schema))]
   :gleam/src "permissions.gleam:43"}
  [user role]
  (cond
    (instance? Editor role) (p/->Ok (->CanEdit user))
    (instance? Owner role) (p/->Ok (->CanEdit user))
    (instance? Viewer role) (p/->Error (->Denied user (->Editor)))))

(defn rename-dashboard
  "rename_dashboard(proof: CanEdit, name: String) -> String

   Demands the proof. Unauthorized calls don't type-check."
  {:malli/schema [:=> [:cat (CanEdit-schema) :string] :string]
   :gleam/src "permissions.gleam:52"}
  ^java.lang.String [proof ^java.lang.String name]
  (let [{{id :value} :value} proof]
    (str "user " (int/to-string id) " renamed dashboard to " name)))

(defn main
  "main() -> Nil"
  {:malli/schema [:=> [:cat] :nil] :gleam/src "permissions.gleam:57"}
  []
  (let [subject (parse-user-id "42")]
    (if (instance? gleam.prelude.Error subject)
      (let [e (:value subject)]
        (io/println (str "bad id: " (string/inspect e))))
      (let [alice (:value subject) subject (require-editor alice (->Editor))]
        (if (instance? Ok subject)
          (let [proof (:value subject)]
            (io/println (rename-dashboard proof "Q3 revenue")))
          (let [needs (:needs (:value subject))]
            (io/println (str "denied: needs " (string/inspect needs)))))))))

(defn require-editor-of
  "require_editor_of(user: UserId, role: Role, dash: DashboardId) -> Result(CanEditDashboard, Denied)

   Scoped proof: rights to one dashboard, not dashboards in general."
  {:malli/schema [:=> [:cat (UserId-schema) (Role-schema) (DashboardId-schema)]
                      (p/result-of (CanEditDashboard-schema) (Denied-schema))]
   :gleam/src "permissions.gleam:78"}
  [user role dash]
  (cond
    (instance? Editor role) (p/->Ok (->CanEditDashboard user dash))
    (instance? Owner role) (p/->Ok (->CanEditDashboard user dash))
    (instance? Viewer role) (p/->Error (->Denied user (->Editor)))))

(defn rename-scoped
  "rename_scoped(proof: CanEditDashboard, name: String) -> String

   No dashboard argument: the proof names the target, so the check and
   the action cannot disagree about which dashboard is meant."
  {:malli/schema [:=> [:cat (CanEditDashboard-schema) :string] :string]
   :gleam/src "permissions.gleam:92"}
  ^java.lang.String [proof ^java.lang.String name]
  (let [{{id :value} :user {d :value} :dash} proof]
    (str "user " (int/to-string id) " renamed dashboard " (int/to-string d) " to " name)))

(defn -main [& _]
  (main))
