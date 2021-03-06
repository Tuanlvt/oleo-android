package com.framgia.oleo.screen.boxchat

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.framgia.oleo.base.BaseViewModel
import com.framgia.oleo.data.source.MessagesRepository
import com.framgia.oleo.data.source.UserRepository
import com.framgia.oleo.data.source.model.BoxChat
import com.framgia.oleo.data.source.model.Message
import com.framgia.oleo.data.source.model.User
import com.framgia.oleo.utils.Index
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList
import javax.inject.Inject

class BoxChatViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val messagesRepository: MessagesRepository
) : BaseViewModel() {

    val boxChatName : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    private val user = userRepository.getUser()
    var newMessageLiveData = MutableLiveData<Message>()
    var oldMessagesLiveData = MutableLiveData<ArrayList<Message>>()
    var imageProfileLiveData = MutableLiveData<String>()
    private var oldMessageId = ""

    fun getMessage(roomId: String) {
        messagesRepository.getMessage(user!!.id, roomId, object : ChildEventListener {
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onCancelled(snapshot: DatabaseError) {}

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val post = snapshot.getValue(Message::class.java)
                if (post != null) newMessageLiveData.value = post
                if (previousChildName == null) oldMessageId = snapshot.key.toString()
            }
        })
    }

    fun loadOldMessage(roomId: String){
        val messageList = arrayListOf<Message>()
        messagesRepository.getOldMessage(user!!.id, roomId, oldMessageId, object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEachIndexed { index, dataSnapshot ->
                    if (index == Index.POSITION_ZERO) oldMessageId = dataSnapshot.key.toString()
                    val post = dataSnapshot.getValue(Message::class.java)
                    if (post != null) messageList.add(post)
                }
                if (messageList.size != Index.POSITION_ZERO) oldMessagesLiveData.value = messageList
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    fun sendMessage(text: String, boxChat: BoxChat) {
        val messageId = messagesRepository.getMessageId(user!!.id, boxChat.id!!)
        val message = Message(messageId, user.id, text, System.currentTimeMillis())
        messagesRepository.sendMessage(user, boxChat, message)
    }

    fun getFriendImageProfile(userId: String) {
        messagesRepository.getImageProfile(userId, object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapShot: DataSnapshot) {
                if (dataSnapShot.exists()) {
                    val user = dataSnapShot.getValue(User::class.java)
                    imageProfileLiveData.value = user!!.image
                }
            }
        })
    }

    fun getBoxChatName(boxChatId : String){
        messagesRepository.getNameBoxChat(userRepository.getUser()!!.id,
                                          boxChatId, object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(data: DataSnapshot) {
                if (data.exists())
                    boxChatName.value = data.getValue(BoxChat::class.java)!!.userFriendName
            }
        })
    }

    fun getUserId() = userRepository.getUser()

    companion object {

        const val LIMIT_DATA = 10

        fun create(fragment: Fragment, factory: ViewModelProvider.Factory): BoxChatViewModel =
            ViewModelProvider(fragment, factory).get(BoxChatViewModel::class.java)
    }
}
