package com.erbe.projectmanager

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.erbe.projectmanager.adapters.TaskItemAdapter
import com.erbe.projectmanager.firebase.FireStoreHandler
import com.erbe.projectmanager.models.Board
import com.erbe.projectmanager.models.Card
import com.erbe.projectmanager.models.Task
import com.erbe.projectmanager.models.User
import com.erbe.projectmanager.utils.Constants
import kotlinx.android.synthetic.main.activity_task_list.*
import java.text.FieldPosition

class TaskListActivity : BaseActivity() {

    companion object {
        const val MEMBERS_REQUEST_CODE : Int = 13
        const val CARD_DETAILS_REQUEST_CODE : Int = 14
    }

    private lateinit var mBoardDetails : Board
    private lateinit var mBoardDocumentID : String
    lateinit var mAssignedMembersDetailList : ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentID = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }

        showProgressDialog()

        FireStoreHandler().getBoardDetails(this, mBoardDocumentID)
    }

    fun boardDetails(board : Board) {
        mBoardDetails = board
        hideProgressDialog()
        setActionBar()

        showProgressDialog()
        FireStoreHandler().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && resultCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE) {
            showProgressDialog()
            FireStoreHandler().getBoardDetails(this, mBoardDocumentID)
        }
        else {
            Log.e("TAG", "onActivityResult: Cancelled", )
        }
    }

    private fun setActionBar(){
        setSupportActionBar(task_list_activity_toolbar)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white)
            actionBar.title = mBoardDetails.name
        }
        task_list_activity_toolbar.setNavigationOnClickListener{ onBackPressed() }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        showProgressDialog()
        FireStoreHandler().getBoardDetails(this, mBoardDetails.documentID)
    }

    fun createTaskList(taskName : String) {
        val task = Task(taskName, FireStoreHandler().getCurrentUserID())
        mBoardDetails.taskList.add(0, task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog()
        FireStoreHandler().addUpdateTaskList(this, mBoardDetails)
    }

    fun updateTaskList(position: Int, listName : String, model : Task) {
        val task = Task(listName, model.createdBy)
        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog()
        FireStoreHandler().addUpdateTaskList(this, mBoardDetails)
    }

    fun deleteTaskList(position : Int) {
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog()
        FireStoreHandler().addUpdateTaskList(this, mBoardDetails)
    }

    fun addCardToTask(position : Int, cardName: String) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        val cardAssignedUsersList : ArrayList<String> = ArrayList()
        val currentUserID = FireStoreHandler().getCurrentUserID()
        cardAssignedUsersList.add(currentUserID)
        val card = Card(cardName, currentUserID, cardAssignedUsersList)

        val cardList = mBoardDetails.taskList[position].cards
        cardList.add(card)

        val task = Task(mBoardDetails.taskList[position].title,
        mBoardDetails.taskList[position].createdBy, cardList)

        mBoardDetails.taskList[position] = task

        showProgressDialog()
        FireStoreHandler().addUpdateTaskList(this, mBoardDetails)
    }


    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this@TaskListActivity, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    fun boardMembersDetailList(list : ArrayList<User>) {
        mAssignedMembersDetailList = list
        hideProgressDialog()

        val addTaskList = Task("Add List")
        mBoardDetails.taskList.add(addTaskList)

        task_list_rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        task_list_rv.setHasFixedSize(true)

        val adapter = TaskItemAdapter(this, mBoardDetails.taskList)
        task_list_rv.adapter = adapter
    }

    fun updateCardsInTaskList(taskListPosition : Int, cards : ArrayList<Card>) {

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        mBoardDetails.taskList[taskListPosition].cards = cards

        showProgressDialog()
        FireStoreHandler().addUpdateTaskList(this, mBoardDetails)
    }
}