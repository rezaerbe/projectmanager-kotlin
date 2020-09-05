package com.erbe.projectmanager

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.GridLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.GridLayoutManager
import com.erbe.projectmanager.adapters.CardMemberListItemAdapter
import com.erbe.projectmanager.dialogs.LabelColorListDialog
import com.erbe.projectmanager.dialogs.MembersListDialog
import com.erbe.projectmanager.firebase.FireStoreHandler
import com.erbe.projectmanager.models.*
import com.erbe.projectmanager.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails : Board
    private lateinit var mMemberDetailList : ArrayList<User>
    private var mTaskListPosition = -1
    private var mCardListPosition = -1
    private var mSelectedColor = ""
    private var mSelectedDueDateMS : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()
        setActionBar()

        name_card_details_et.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
        name_card_details_et.setSelection(name_card_details_et.text.toString().length)

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].labelColor
        if (mSelectedColor.isNotEmpty()) {
            setColor()
        }

        update_card_details_btn.setOnClickListener {
            if (name_card_details_et.text.toString().isNotEmpty()) {
                updateCardDetails()
            }
            else {
                Toast.makeText(this, "Please enter a card name", Toast.LENGTH_SHORT).show()
            }
        }

        select_label_color_tv.setOnClickListener {
            labelColorsListDialog()
        }

        select_members_tv.setOnClickListener {
            membersListDialog()
        }

        mSelectedDueDateMS = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].dueDate

        if (mSelectedDueDateMS > 0) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMS))
            select_due_date_tv.text = selectedDate
        }

        select_due_date_tv.setOnClickListener {
            showDatePicker()
        }

        setupSelectedMembersList()
    }

    private fun setActionBar() {
        setSupportActionBar(toolbar_card_details_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white)
            actionBar.title =
                mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name
        }
        toolbar_card_details_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getIntentData() {

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }

        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardListPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }

        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mMemberDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateCardDetails() {
        val card = Card(
            name_card_details_et.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMS
        )

        val taskList : ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition] = card

        showProgressDialog()
        FireStoreHandler().addUpdateTaskList(this, mBoardDetails)
    }

    private fun membersListDialog() {
        var cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo

        if (cardAssignedMembersList.size > 0) {
            for (i in mMemberDetailList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMemberDetailList[i].id == j) {
                        mMemberDetailList[i].selected = true
                    }
                }
            }
        }
        else {
            for (i in mMemberDetailList.indices) {
                mMemberDetailList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(this, mMemberDetailList, "Select Members") {
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.contains((user.id))) {
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.add(user.id)
                    }
                }
                else {
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.remove(user.id)

                    for (i in mMemberDetailList.indices) {
                        if (mMemberDetailList[i].id == user.id) {
                            mMemberDetailList[i].selected = false
                        }
                    }
                }

                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    private fun deleteCard() {
        val cardList : ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardList.removeAt(mCardListPosition)

        val taskList : ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        taskList[mTaskListPosition].cards = cardList

        showProgressDialog()
        FireStoreHandler().addUpdateTaskList(this, mBoardDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_delete_card -> {
                val cardTitle = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name
                alertDialogForDeleteCard(cardTitle)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun alertDialogForDeleteCard(cardName : String) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Alert!")
        builder.setMessage("Are you sure you want to delete $cardName?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes") {
            dialog, which ->
            dialog.dismiss()
            deleteCard()
        }

        builder.setNegativeButton("No") {
            dialog, which ->
            dialog.dismiss()
        }

        val alertDialog : AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun colorsList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#90F700")
        colorsList.add("#8400F7")
        colorsList.add("#F700CE")
        colorsList.add("#FF9800")
        colorsList.add("#7A8089")

        return colorsList
    }

    private fun setColor() {
        select_label_color_tv.text = ""
        select_label_color_tv.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun labelColorsListDialog() {
        val colorList : ArrayList<String> = colorsList()
        val listDialog = object : LabelColorListDialog(this, colorList, "Select Label Color", mSelectedColor) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun setupSelectedMembersList() {
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo
        val selectedMembersList : ArrayList<SelectedMembers> = ArrayList()

        for (i in mMemberDetailList.indices) {
            for (j in cardAssignedMembersList) {
                if (mMemberDetailList[i].id == j) {
                    val selectedMembers = SelectedMembers(mMemberDetailList[i].id, mMemberDetailList[i].image)
                    selectedMembersList.add(selectedMembers)
                }
            }
        }

        if (selectedMembersList.size > 0) {
            selectedMembersList.add(SelectedMembers("", ""))
            select_members_tv.visibility = View.GONE
            selected_members_list_rv.visibility = View.VISIBLE

            selected_members_list_rv.layoutManager = GridLayoutManager(this, 6)

            val adapter = CardMemberListItemAdapter(this, selectedMembersList, true)
            selected_members_list_rv.adapter = adapter

            adapter.setOnClickListener(object : CardMemberListItemAdapter.OnClickListener {
                override fun onClick() {
                    membersListDialog()
                }
            })
        }
        else {
            select_members_tv.visibility = View.VISIBLE
            selected_members_list_rv.visibility = View.GONE
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialogListener = DatePickerDialog.OnDateSetListener{
            view, year, monthOfYear, dayOfMonth ->
            val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
            val sMonthOfYear = if ((monthOfYear + 1) < 10) "0${monthOfYear+1}" else "${monthOfYear+1}"

            val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
            select_due_date_tv.text = selectedDate

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val theDate = sdf.parse(selectedDate)
            mSelectedDueDateMS = theDate!!.time
        }

        val datePickerDialog = DatePickerDialog(this, datePickerDialogListener, year, month, day)
        datePickerDialog.show()
    }
}