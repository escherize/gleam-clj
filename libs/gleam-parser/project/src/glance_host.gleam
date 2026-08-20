// Host module so `gleam build` fetches glance; the library surface is the
// compiled glance module itself plus the Clojure wrapper.
import glance

pub fn parse(src: String) -> Result(glance.Module, glance.Error) {
  glance.module(src)
}
