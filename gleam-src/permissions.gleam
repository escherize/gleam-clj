import gleam/int
import gleam/io
import gleam/string

pub opaque type UserId {
  UserId(Int)
}

pub type IdError {
  Empty
  NotANumber(String)
  OutOfRange(Int)
}

/// Parse untrusted input into a UserId — the only way to make one.
pub fn parse_user_id(raw: String) -> Result(UserId, IdError) {
  case string.trim(raw) {
    "" -> Error(Empty)
    trimmed ->
      case int.parse(trimmed) {
        Error(_) -> Error(NotANumber(trimmed))
        Ok(n) if n < 1 -> Error(OutOfRange(n))
        Ok(n) -> Ok(UserId(n))
      }
  }
}

pub type Role {
  Viewer
  Editor
  Owner
}

pub opaque type CanEdit {
  CanEdit(UserId)
}

pub type Denied {
  Denied(who: UserId, needs: Role)
}

/// Check once, at the boundary. Success mints the proof.
pub fn require_editor(user: UserId, role: Role) -> Result(CanEdit, Denied) {
  case role {
    Editor -> Ok(CanEdit(user))
    Owner -> Ok(CanEdit(user))
    Viewer -> Error(Denied(user, Editor))
  }
}

/// Demands the proof. Unauthorized calls don't type-check.
pub fn rename_dashboard(proof: CanEdit, name: String) -> String {
  let CanEdit(UserId(id)) = proof
  "user " <> int.to_string(id) <> " renamed dashboard to " <> name
}

pub fn main() {
  case parse_user_id("42") {
    Error(e) -> io.println("bad id: " <> string.inspect(e))
    Ok(alice) ->
      case require_editor(alice, Editor) {
        Ok(proof) -> io.println(rename_dashboard(proof, "Q3 revenue"))
        Error(Denied(_, needs)) ->
          io.println("denied: needs " <> string.inspect(needs))
      }
  }
}
