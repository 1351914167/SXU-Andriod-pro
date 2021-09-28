package com.zsh.sight.Utils;

import androidx.fragment.app.Fragment;

public interface NavigationHost {
    void navigateTo(Fragment fragment, boolean addToBackstack);
}
