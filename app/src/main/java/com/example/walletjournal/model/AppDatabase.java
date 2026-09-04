package com.example.walletjournal.model;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Account.class, Record.class, Budget.class, Category.class}, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract AccountDao accountDao();

    public abstract RecordDao recordDao();

    public abstract BudgetDao budgetDao();

    public abstract CategoryDao categoryDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "wallet_journal.db")
                            .addCallback(SEED_CATEGORIES_CALLBACK)
                            // versions 1-5 predate real migrations for this app — those old,
                            // already-superseded schemas can still be wiped and rebuilt safely.
                            // From version 6 onward, every bump MUST ship a real Migration
                            // below (via addMigrations) so accounts/records/budgets/categories
                            // survive the upgrade. If a future version is missing its Migration,
                            // Room throws instead of silently deleting the user's data.
                            .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5)
                            .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
                            // .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, ...)  // append future migrations here
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Ensures the built-in EXPENSE and INCOME categories exist every time the
     * database is opened (not just when it's first created) — so they reappear
     * even after a destructive migration wipes the table, instead of only being
     * seeded once and then lost for good on the next schema change. EXPENSE and
     * INCOME are checked (and seeded) independently, since MIGRATION_6_7 tags
     * every pre-existing row EXPENSE, which would otherwise make the table look
     * non-empty and skip seeding INCOME's rows on upgrade.
     */
    private static final RoomDatabase.Callback SEED_CATEGORIES_CALLBACK = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            if (isEmpty(db, "EXPENSE")) {
                insertCategory(db, "餐飲", "bars", 0, "EXPENSE");
                insertCategory(db, "交通", "bus", 2, "EXPENSE");
                insertCategory(db, "購物", "transfer", 3, "EXPENSE");
                insertCategory(db, "娛樂", "card", 4, "EXPENSE");
                insertCategory(db, "醫療", "camera", 2, "EXPENSE");
                insertCategory(db, "其他", "house", 6, "EXPENSE");
            }
            if (isEmpty(db, "INCOME")) {
                insertCategory(db, "薪資", "card", 3, "INCOME");
                insertCategory(db, "獎金", "bars", 0, "INCOME");
                insertCategory(db, "其他", "house", 6, "INCOME");
            }
        }

        private boolean isEmpty(SupportSQLiteDatabase db, String type) {
            try (Cursor cursor = db.query("SELECT COUNT(*) FROM categories WHERE type = '" + type + "'")) {
                return cursor.moveToFirst() && cursor.getInt(0) == 0;
            }
        }

        private void insertCategory(SupportSQLiteDatabase db, String name, String icon, int colorIndex, String type) {
            db.execSQL("INSERT INTO categories (name, icon, colorIndex, type) VALUES ('"
                    + name + "', '" + icon + "', " + colorIndex + ", '" + type + "')");
        }
    };

    /**
     * Existing categories all predate the type split — they were EXPENSE-only.
     * Category.type has no @NonNull/@ColumnInfo(defaultValue=...), so Room's
     * expected schema for this column is nullable with no SQL-level default;
     * the ADD COLUMN here must match that exactly (a NOT NULL DEFAULT clause
     * fails Room's post-migration schema validation with "Migration didn't
     * properly handle: categories"). Old rows are backfilled via UPDATE
     * instead, which only touches data, not the column definition.
     */
    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE categories ADD COLUMN type TEXT");
            db.execSQL("UPDATE categories SET type = 'EXPENSE' WHERE type IS NULL");
        }
    };

    /**
     * Adds the two indices declared on Record (see its class doc): (createdAt, id) for
     * RecordDao#getAll()'s sort, (type, createdAt) for the Stats screen's range-filtered
     * totals. Names must match exactly what Room's annotation processor generates for
     * those @Index columns, or schema validation fails on next open ("Migration didn't
     * properly handle: records") — Room's default naming is index_<table>_<col1>_<col2>.
     */
    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_records_createdAt_id` ON `records` (`createdAt`, `id`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_records_type_createdAt` ON `records` (`type`, `createdAt`)");
        }
    };

    /*
     * TEMPLATE for the next schema change — copy this shape, don't bump
     * @Database's version without adding the matching Migration below AND
     * registering it via .addMigrations(...) above.
     *
     * static final Migration MIGRATION_8_9 = new Migration(8, 9) {
     *     @Override
     *     public void migrate(@NonNull SupportSQLiteDatabase db) {
     *         // Additive changes only touch what's new — everything else stays as-is:
     *         db.execSQL("ALTER TABLE accounts ADD COLUMN note TEXT");
     *
     *         // New table: just CREATE it (match Room's generated column names/types
     *         // exactly, or a schema-validation check will fail at runtime).
     *         // db.execSQL("CREATE TABLE IF NOT EXISTS new_table (...)");
     *     }
     * };
     */
}
