package com.example.hashpay.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hashpay.data.database.daos.ContactDao
import com.example.hashpay.data.database.daos.InvoiceDao
import com.example.hashpay.data.database.daos.TransactionDao
import com.example.hashpay.data.database.entities.Contact
import com.example.hashpay.data.database.entities.Invoice
import com.example.hashpay.data.database.entities.Transaction

@Database(
    entities = [Transaction::class, Contact::class, Invoice::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun contactDao(): ContactDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hashpay_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}