package com.example.taller2

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView

class ContactsAdapter(context: Context?, c: Cursor?, flags: Int) : CursorAdapter(context, c, flags) {
    private val CONTACT_ID_INDEX = 0
    private val DISPLAY_NAME_INDEX = 1

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.activity_contacts_adapter, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val contactIDView = view?.findViewById<TextView>(R.id.contactID)
        val nameView = view?.findViewById<TextView>(R.id.name)
        val numberID = cursor?.getInt(CONTACT_ID_INDEX)
        val name = cursor?.getString(DISPLAY_NAME_INDEX)
        contactIDView?.text = numberID?.toString()
        nameView?.text = name
    }
}