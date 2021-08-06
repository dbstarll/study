package io.github.dbstarll.study.entity.enums;

import io.github.dbstarll.study.entity.Principal;

import java.util.*;

public enum Page {
  // ENGLISH
  // word(Module.ENGLISH),

  book(Module.ENGLISH),

  exercise_book(Module.ENGLISH, Principal.Mode.USER),

  spell(Module.ENGLISH, Principal.Mode.USER, Principal.Mode.GUEST),

  spell_exchange(Module.ENGLISH, Principal.Mode.USER),

  // listen(Module.ENGLISH, Mode.USER),

  // read(Module.ENGLISH, Mode.USER),

  // MATH
  // custom(Module.MATH, Mode.USER),

  // mix(Module.MATH, Mode.USER, Mode.GUEST),

  // SETTING

  user(Module.SETTING);

  // approve(Module.SETTING);

  public final Module module;

  private final Set<Principal.Mode> modes;

  private Page(final Module module, final Principal.Mode... modes) {
    this.module = module;
    this.modes = new HashSet<>(Arrays.asList(modes));
  }

  public Collection<Principal.Mode> modes() {
    return Collections.unmodifiableCollection(modes);
  }

  public boolean mode(Principal.Mode mode) {
    return modes.contains(mode);
  }
}
