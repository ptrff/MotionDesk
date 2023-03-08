package ru.ptrff.motiondesk.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects")
public class Project {
    @PrimaryKey(autoGenerate = true)
    int id;
    private final String name;
    private final String description;
    private final float stars;

    public Project(String name, String description, float stars) {
        this.name = name;
        this.description = description;
        this.stars = stars;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", stars=" + stars +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public float getStars() {
        return stars;
    }
}
