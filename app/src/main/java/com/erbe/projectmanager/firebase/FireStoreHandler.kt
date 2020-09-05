package com.erbe.projectmanager.firebase

import android.app.Activity
import android.widget.Toast
import com.erbe.projectmanager.*
import com.erbe.projectmanager.models.Board
import com.erbe.projectmanager.models.Task
import com.erbe.projectmanager.models.User
import com.erbe.projectmanager.utils.Constants
import com.google.firebase.auth.AdditionalUserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.lang.reflect.Member

class FireStoreHandler {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
    }

    fun getCurrentUserID() : String {

        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun loadUserData(activity: Activity, readBoardsList : Boolean = false) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!
                when(activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }
            }.addOnFailureListener {
                when(activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
            }
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                when(activity) {
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }
            }.addOnFailureListener {
                when(activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Toast.makeText(activity, "Failed updating profile", Toast.LENGTH_SHORT).show()
            }
    }

    fun getBoardsList(activity: MainActivity) {
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener {
                document ->
                val boardsList : ArrayList<Board> = ArrayList()
                for (i in document.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentID = i.id
                    boardsList.add(board)
                }
                activity.populateBoardsToUI(boardsList)
            }.addOnFailureListener {
                activity.hideProgressDialog()
            }
    }

    fun createBoard(activity: CreateBoardActivity, boardInfo : Board) {
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(boardInfo, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity,"Board created successfully", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                activity.hideProgressDialog()
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentID : String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentID)
            .get()
            .addOnSuccessListener {
                document ->
                val board = document.toObject(Board::class.java)!!
                board.documentID = document.id
                activity.boardDetails(board)
            }.addOnFailureListener {
                activity.hideProgressDialog()
            }
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo : ArrayList<String>) {
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {
                document ->
                val usersList : ArrayList<User> = ArrayList()

                for (i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }

                if (activity is MembersActivity) {
                    activity.setUpMembersList(usersList)
                }
                else if (activity is TaskListActivity) {
                    activity.boardMembersDetailList(usersList)
                }
            }.addOnFailureListener { e ->
                if (activity is MembersActivity) {
                    activity.hideProgressDialog()
                }
                else if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
            }
    }

    fun addUpdateTaskList(activity: Activity, board : Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentID)
            .update(taskListHashMap)
            .addOnSuccessListener {
                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                }
                else if (activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }
            }.addOnFailureListener { e ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                else if (activity is CardDetailsActivity) {
                    activity.hideProgressDialog()
                }
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentID)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }.addOnFailureListener {
                e ->
                activity.hideProgressDialog()
            }
    }

    fun getMemberDetails(activity: MembersActivity, email : String) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                document ->
                if (document.size() > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }
                else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }.addOnFailureListener {
                activity.hideProgressDialog()
            }
    }
}