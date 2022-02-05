package com.example.exercisetimer.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import com.example.exercisetimer.CustomDBHandler

class MyContentProvider : ContentProvider()
{
    val exercises = 1;
    val exercises_id = 2;

    private var DBHandler: CustomDBHandler? = null;

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH);

    init {
        uriMatcher.addURI(AUTHORITY, TABLE, exercises)
        uriMatcher.addURI(AUTHORITY, TABLE + "/#",
            exercises_id)
    }

    companion object
    {
        val AUTHORITY = "com.example.exercisetimer.provider.MyContentProvider"
        private val TABLE = "tblExercises"
        val CONTENT_URI : Uri = Uri.parse("content://" + AUTHORITY + "/" +
                TABLE)
    }


    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int
    {
        val uriType = uriMatcher.match(uri); //Matches the uri to the content provider.
        val db = DBHandler!!.writableDatabase;
        when (uriType)
        {
            exercises -> db.delete(CustomDBHandler.TABLE_NAME, selection, selectionArgs); //If single exercise, delete.
            else -> throw IllegalArgumentException("Unknown URI: " + uri) //There should never be a time in this app where multiple records are deleted at once, so any other uri type is invalid.
        }
        context.contentResolver.notifyChange(uri, null)
        return 1;
    }

    override fun getType(uri: Uri): String?
    {
        throw Exception("Not needed.")
    }

    override fun insert(uri: Uri, values: ContentValues): Uri? //Function to build insert statement to be executed by CustomeDBHandler.
    {
        val uriType = uriMatcher.match(uri); //Matches the uni to the content provider.
        val db = DBHandler!!.writableDatabase;
        val id: Long;
        when (uriType)
        {
            exercises -> id = db.insert(CustomDBHandler.TABLE_NAME, null, values); //If a single exercise, insert via the SQLHelper object.
            else -> throw IllegalArgumentException("Unknown URI: " + uri); //If multiple exercises, illegal insert statement.
        }
        context.contentResolver.notifyChange(uri, null);
        return Uri.parse(TABLE + "/" + id) //Return info message to sender.
    }

    override fun onCreate(): Boolean
    {
        DBHandler = CustomDBHandler(context);

        return false;
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?): Cursor? //Function to build query to be executed by CustomeDBHandler.
    {
        val queryBuilder = SQLiteQueryBuilder(); //Builds query.
        queryBuilder.tables = CustomDBHandler.TABLE_NAME;

        val uriType = uriMatcher.match(uri); //Matches the uni to the content provider.

        when (uriType)
        {
            exercises_id -> queryBuilder.appendWhere("name =" + uri.lastPathSegment); //If uri specifies a single row, append row specifier to query.
            exercises -> {} //If uri specifies all rows, don't append anything.
            else -> throw IllegalArgumentException("Unknown URI");
        }

        val cursor = queryBuilder.query(DBHandler?.readableDatabase,
            projection, selection, selectionArgs, null, null, sortOrder); //Create cursor to interface with database.
        cursor.setNotificationUri(context.contentResolver, uri);
        return cursor;
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?): Int //Function to build update statement to be executed by CustomeDBHandler.
    {
        val uriType = uriMatcher.match(uri); //Matches the uri to the content provider.
        val db = DBHandler!!.writableDatabase;
        var l = 99;
        when (uriType)
        {
            exercises -> l=db.update(CustomDBHandler.TABLE_NAME, values, selection, selectionArgs); //If single exercise, update.
            else -> throw IllegalArgumentException("Unknown URI: " + uri) //There should never be a time in this app where multiple records are updated at once, so any other uri type is invalid.
        }
        context.contentResolver.notifyChange(uri, null)
        return 1;
    }
}
