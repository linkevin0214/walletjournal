package com.example.walletjournal.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * A category (built-in or user-created via the Add Category screen), scoped
 * to either EXPENSE or INCOME (RecordType.name()) — the two record types
 * keep separate category lists. Room entity for the "categories" table.
 */
@Entity(tableName = "categories")
public class Category {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;

    /** One of CategoryPalette.ICON_KEYS. */
    private String icon;

    /** Index into CategoryPalette.BASE_COLORS / PASTEL_COLORS. */
    private int colorIndex;

    /** RecordType.EXPENSE.name() or RecordType.INCOME.name(). */
    private String type;

    /** Used by Room to reconstruct rows read from the database. */
    public Category(long id, String name, String icon, int colorIndex, String type) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.colorIndex = colorIndex;
        this.type = type;
    }

    /** Convenience constructor for creating a new (not-yet-saved) category. */
    @Ignore
    public Category(String name, String icon, int colorIndex, String type) {
        this(0, name, icon, colorIndex, type);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public String getType() {
        return type;
    }
}
