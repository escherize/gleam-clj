//! Snapshot tests: emitter stdout must match the checked-in gen/*.clj files.
//! On intentional output changes, regenerate:
//!   cargo run -q -- ../gleam-src/<f>.gleam ../gen/<f>.clj

use std::path::Path;
use std::process::Command;

#[test]
fn snapshots() {
    let root = Path::new(env!("CARGO_MANIFEST_DIR")).parent().unwrap().to_path_buf();
    for fixture in ["coin_change", "shapes", "sum_to", "jellyfish", "ffi_demo", "permissions"] {
        let input = root.join("gleam-src").join(format!("{fixture}.gleam"));
        let expected_path = root.join("gen").join(format!("{fixture}.clj"));
        let expected = std::fs::read_to_string(&expected_path)
            .unwrap_or_else(|e| panic!("read {expected_path:?}: {e}"));
        let out = Command::new(env!("CARGO_BIN_EXE_gleam-to-clj"))
            .arg(&input)
            .current_dir(&root)
            .output()
            .expect("run emitter");
        assert!(out.status.success(), "{fixture}: emitter failed: {}",
            String::from_utf8_lossy(&out.stderr));
        let actual = String::from_utf8(out.stdout).expect("utf8");
        assert_eq!(actual, expected, "{fixture}: output drifted from gen/{fixture}.clj");
    }
}
