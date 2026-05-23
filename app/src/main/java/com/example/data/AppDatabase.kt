package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Booking::class, Message::class, WalletTransaction::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bookingDao(): BookingDao
    abstract fun messageDao(): MessageDao
    abstract fun walletTransactionDao(): WalletTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sajha_trust_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }

            suspend fun populateDatabase(database: AppDatabase) {
                val userDao = database.userDao()
                val transactionDao = database.walletTransactionDao()

                // Insert Pre-verified and Pending local workers
                userDao.insertUser(
                    User(
                        id = "worker_ram",
                        name = "Ram Bahadur Shrestha",
                        phone = "+977 9851012345",
                        role = "WORKER",
                        isVerified = true,
                        verificationRequestStatus = "VERIFIED",
                        rating = 4.8f,
                        completedJobsCount = 52,
                        walletBalance = 3500.0,
                        skills = "Electrician, Wiring, Home Maintenance",
                        profileImage = "electrician",
                        selfieVerified = true
                    )
                )

                userDao.insertUser(
                    User(
                        id = "worker_hari",
                        name = "Hari Prasad Thapa",
                        phone = "+977 9841223344",
                        role = "WORKER",
                        isVerified = true,
                        verificationRequestStatus = "VERIFIED",
                        rating = 4.6f,
                        completedJobsCount = 18,
                        walletBalance = 4200.0,
                        skills = "Plumber, Leak Repair, Pipe Installation",
                        profileImage = "plumber",
                        selfieVerified = true
                    )
                )

                userDao.insertUser(
                    User(
                        id = "worker_sita",
                        name = "Sita Kumari Baral",
                        phone = "+977 9811556677",
                        role = "WORKER",
                        isVerified = false,
                        verificationRequestStatus = "PENDING", // Visible in Admin Dashboard for approvals!
                        rating = 5.0f,
                        completedJobsCount = 0,
                        walletBalance = 0.0,
                        skills = "Mason, Tile Fitting, Concrete repair",
                        profileImage = "mason",
                        selfieVerified = true
                    )
                )

                userDao.insertUser(
                    User(
                        id = "worker_krishna",
                        name = "Krishna BK",
                        phone = "+977 9845778899",
                        role = "WORKER",
                        isVerified = false,
                        verificationRequestStatus = "PENDING", // Visible in Admin Dashboard for approvals!
                        rating = 4.2f,
                        completedJobsCount = 12,
                        walletBalance = 800.0,
                        skills = "Carpenter, Door Fix, Furniture carpentry",
                        profileImage = "carpenter",
                        selfieVerified = true
                    )
                )

                // Insert Customers with default balances
                userDao.insertUser(
                    User(
                        id = "customer_shyam",
                        name = "Shyam Lal Nepal",
                        phone = "+977 9801889922",
                        role = "CUSTOMER",
                        isVerified = true,
                        verificationRequestStatus = "VERIFIED",
                        rating = 5.0f,
                        completedJobsCount = 4,
                        walletBalance = 15000.0, // Preloaded with funds
                        skills = "",
                        profileImage = "customer_avatar",
                        selfieVerified = true
                    )
                )

                userDao.insertUser(
                    User(
                        id = "admin_super",
                        name = "System Admin Moderator",
                        phone = "+977 9851001100",
                        role = "ADMIN",
                        isVerified = true,
                        verificationRequestStatus = "VERIFIED"
                    )
                )

                // Populate initial history transactions
                transactionDao.insertTransaction(
                    WalletTransaction(
                        userId = "customer_shyam",
                        amount = 15000.0,
                        provider = "eSewa",
                        txType = "DEPOSIT",
                        status = "SUCCESS",
                        referenceId = "ESW-DEP-102941"
                    )
                )
            }
        }
    }
}
