import gleam/io

pub type Fish {
  Starfish(name: String, favourite_color: String)
  Jellyfish(name: String, jiggly: Bool)
}

fn describe(fish: Fish) -> String {
  case fish {
    Starfish(name, color) -> name <> " likes the color " <> color
    Jellyfish(name, True) -> name <> " is jiggly!"
    Jellyfish(name, False) -> name <> " is not jiggly"
  }
}

pub fn main() {
  io.println(describe(Starfish("Sandy", "pink")))
  io.println(describe(Jellyfish("Jelly", True)))
  io.println(describe(Jellyfish("Bob", False)))
}
