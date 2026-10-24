package com.example.myapplication.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.myapplication.data.db.DatabaseHelper
import com.example.myapplication.data.model.Account
import com.example.myapplication.data.model.Employee
import com.example.myapplication.data.model.Salary
import com.example.myapplication.data.model.WorkLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class AppRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // =========================================
    // ACCOUNT OPERATIONS
    // =========================================

    fun authenticate(username: String, password: String): Account? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ACCOUNT,
            null,
            "${DatabaseHelper.ACCOUNT_USERNAME} = ? AND ${DatabaseHelper.ACCOUNT_PASSWORD} = ?",
            arrayOf(username, password),
            null, null, null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_ID))
                val u = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_USERNAME))
                val p = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_PASSWORD))
                val r = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_ROLE))
                return Account(id, u, p, r)
            }
        }
        return null
    }

    fun getAccountById(id: Long): Account? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ACCOUNT,
            null,
            "${DatabaseHelper.ACCOUNT_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val u = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_USERNAME))
                val p = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_PASSWORD))
                val r = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_ROLE))
                return Account(id, u, p, r)
            }
        }
        return null
    }

    // =========================================
    // EMPLOYEE OPERATIONS
    // =========================================

    fun getAllEmployees(query: String = ""): List<Employee> {
        val list = mutableListOf<Employee>()
        val db = dbHelper.readableDatabase
        val selection = if (query.isBlank()) null else {
            "${DatabaseHelper.EMP_FULLNAME} LIKE ? OR ${DatabaseHelper.EMP_CODE} LIKE ?"
        }
        val selectionArgs = if (query.isBlank()) null else {
            arrayOf("%$query%", "%$query%")
        }

        val cursor = db.query(
            DatabaseHelper.TABLE_EMPLOYEE,
            null,
            selection,
            selectionArgs,
            null, null,
            "${DatabaseHelper.EMP_CODE} ASC"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.EMP_ID))
                val code = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_CODE))
                val name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_FULLNAME))
                val email = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_EMAIL))
                val phone = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_PHONE))
                val rate = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.EMP_HOURLY_RATE))
                val accIdIdx = c.getColumnIndexOrThrow(DatabaseHelper.EMP_ACCOUNT_ID)
                val accId = if (c.isNull(accIdIdx)) null else c.getLong(accIdIdx)

                list.add(Employee(id, code, name, email, phone, rate, accId))
            }
        }
        return list
    }

    fun getEmployeeById(id: Long): Employee? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_EMPLOYEE,
            null,
            "${DatabaseHelper.EMP_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val code = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_CODE))
                val name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_FULLNAME))
                val email = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_EMAIL))
                val phone = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_PHONE))
                val rate = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.EMP_HOURLY_RATE))
                val accIdIdx = c.getColumnIndexOrThrow(DatabaseHelper.EMP_ACCOUNT_ID)
                val accId = if (c.isNull(accIdIdx)) null else c.getLong(accIdIdx)
                return Employee(id, code, name, email, phone, rate, accId)
            }
        }
        return null
    }

    fun getEmployeeByAccountId(accountId: Long): Employee? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_EMPLOYEE,
            null,
            "${DatabaseHelper.EMP_ACCOUNT_ID} = ?",
            arrayOf(accountId.toString()),
            null, null, null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.EMP_ID))
                val code = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_CODE))
                val name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_FULLNAME))
                val email = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_EMAIL))
                val phone = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_PHONE))
                val rate = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.EMP_HOURLY_RATE))
                return Employee(id, code, name, email, phone, rate, accountId)
            }
        }
        return null
    }

    fun addEmployeeWithAccount(
        code: String,
        fullname: String,
        email: String,
        phone: String,
        hourlyRate: Double,
        username: String,
        password: String
    ): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Insert Account
            val accValues = ContentValues().apply {
                put(DatabaseHelper.ACCOUNT_USERNAME, username)
                put(DatabaseHelper.ACCOUNT_PASSWORD, password)
                put(DatabaseHelper.ACCOUNT_ROLE, "EMPLOYEE")
            }
            val accId = db.insert(DatabaseHelper.TABLE_ACCOUNT, null, accValues)
            if (accId == -1L) return false

            // Insert Employee
            val empValues = ContentValues().apply {
                put(DatabaseHelper.EMP_CODE, code)
                put(DatabaseHelper.EMP_FULLNAME, fullname)
                put(DatabaseHelper.EMP_EMAIL, email)
                put(DatabaseHelper.EMP_PHONE, phone)
                put(DatabaseHelper.EMP_HOURLY_RATE, hourlyRate)
                put(DatabaseHelper.EMP_ACCOUNT_ID, accId)
            }
            val empId = db.insert(DatabaseHelper.TABLE_EMPLOYEE, null, empValues)
            if (empId == -1L) return false

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun updateEmployee(employee: Employee): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.EMP_CODE, employee.code)
            put(DatabaseHelper.EMP_FULLNAME, employee.fullname)
            put(DatabaseHelper.EMP_EMAIL, employee.email)
            put(DatabaseHelper.EMP_PHONE, employee.phone)
            put(DatabaseHelper.EMP_HOURLY_RATE, employee.hourlyRate)
        }
        val count = db.update(
            DatabaseHelper.TABLE_EMPLOYEE,
            values,
            "${DatabaseHelper.EMP_ID} = ?",
            arrayOf(employee.id.toString())
        )
        return count > 0
    }

    fun deleteEmployee(employeeId: Long): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val emp = getEmployeeById(employeeId)
            if (emp?.accountId != null) {
                db.delete(
                    DatabaseHelper.TABLE_ACCOUNT,
                    "${DatabaseHelper.ACCOUNT_ID} = ?",
                    arrayOf(emp.accountId.toString())
                )
            }
            val count = db.delete(
                DatabaseHelper.TABLE_EMPLOYEE,
                "${DatabaseHelper.EMP_ID} = ?",
                arrayOf(employeeId.toString())
            )
            db.setTransactionSuccessful()
            return count > 0
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }

}
