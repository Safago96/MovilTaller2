package com.example.taller2

import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class Contacts : AppCompatActivity() {
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var contactsList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        contactsList = findViewById(R.id.contactsList)

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
            ),
            null,            null,            null
        )

        contactsAdapter = ContactsAdapter(this, cursor, 0)
        contactsList.adapter = contactsAdapter
    }
}