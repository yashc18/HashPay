package com.example.hashpay.data.repositories

import com.example.hashpay.data.database.daos.ContactDao
import com.example.hashpay.data.database.entities.Contact
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()
    val favoriteContacts: Flow<List<Contact>> = contactDao.getFavoriteContacts()

    fun getRecentContacts(limit: Int): Flow<List<Contact>> {
        return contactDao.getRecentContacts(limit)
    }

    suspend fun insert(contact: Contact): Long {
        return contactDao.insert(contact)
    }

    suspend fun update(contact: Contact) {
        contactDao.update(contact)
    }

    suspend fun delete(contact: Contact) {
        contactDao.delete(contact)
    }

    suspend fun getContactByAddress(address: String): Contact? {
        return contactDao.getContactByAddress(address)
    }

    suspend fun updateLastTransactionDate(address: String, timestamp: Long) {
        contactDao.updateLastTransactionDate(address, timestamp)
    }
}