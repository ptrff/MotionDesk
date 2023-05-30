package ru.ptrff.motiondesk.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.ptrff.motiondesk.view.AuthFragment;
import ru.ptrff.motiondesk.view.EditProfileFragment;
import ru.ptrff.motiondesk.view.BrowseFragment;
import ru.ptrff.motiondesk.view.LibFragment;
import ru.ptrff.motiondesk.view.ProfileFragment;

public class MainViewModel extends AndroidViewModel {
    private final MutableLiveData<Fragment> currentFragment = new MutableLiveData<>();
    private int previousFragmentId = 0;
    private int currentFragmentId = 0;
    private final LibFragment libFragment;
    private final BrowseFragment browseFragment;
    private final ProfileFragment profileFragment;

    public MainViewModel(@NonNull Application application) {
        super(application);
        libFragment = new LibFragment();
        browseFragment = new BrowseFragment();
        profileFragment = new ProfileFragment();
        currentFragment.setValue(libFragment);
    }

    public void setCurrentFragment(int currentFragmentId) {
        Fragment fragment;
        previousFragmentId = this.currentFragmentId;
        this.currentFragmentId=currentFragmentId;
        switch (currentFragmentId) {
            case 1:
                fragment = browseFragment;
                break;
            case 2:
                fragment = profileFragment;
                break;
            case 3:
            case 4:
                fragment = new EditProfileFragment();
                break;
            default:
                fragment=libFragment;
                break;
        }
        currentFragment.setValue(fragment);
    }

    public LibFragment getLibFragment(){
        return libFragment;
    }

    public LiveData<Fragment> getCurrentFragment() {
        return currentFragment;
    }

    public int getPreviousFragmentId() {
        return previousFragmentId;
    }

    public int getCurrentFragmentId(){
        return currentFragmentId;
    }
}
