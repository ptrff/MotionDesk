package ru.ptrff.motiondesk.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class IDGenerator {
    private static final int ID_LENGTH = 9;
    public static final Set<String> usedIDs = new HashSet<>();

    public static String generateID() {
        Random random = new Random();
        String id;

        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ID_LENGTH; i++) {
                sb.append(random.nextInt(10));
            }
            id = sb.toString();
        } while (usedIDs.contains(id));

        usedIDs.add(id);
        return id;
    }
}