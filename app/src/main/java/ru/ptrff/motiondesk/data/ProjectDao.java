package ru.ptrff.motiondesk.data;

import io.reactivex.Completable;
import io.reactivex.Observable;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ProjectDao {

    @Insert
    Completable addProject(Project project);

    @Query("SELECT * FROM projects WHERE id = :id")
    Observable<Project> getProject(int id);

}
