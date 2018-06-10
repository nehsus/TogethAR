package io.agora.agoravideodemo.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.telephony.TelephonyManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.github.nitrico.lastadapter.LastAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.agora.agoravideodemo.BR
import io.agora.agoravideodemo.FirebasePhoneNumAuthActivity
import io.agora.agoravideodemo.R
import io.agora.agoravideodemo.base.BaseRtcActivity
import io.agora.agoravideodemo.databinding.ItemContactBinding
import io.agora.agoravideodemo.model.*
import io.agora.agoravideodemo.ui.VideoChatViewActivity.CHAT_ROOM_KEY
import io.agora.agoravideodemo.utils.*
import io.agora.rtc.RtcEngine
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_contact.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.lang.ref.WeakReference


class MainActivity : BaseRtcActivity(), ContactsPullTask.ContactsPullTaskInteractionListener {

    private val contactsAdapter: LastAdapter by lazy { initLastAdapter() }
    private val mEventBus = EventBus.getDefault()

    private var listOfLocalUsers: List<ContactModel>? = null
    private var listOfRegisteredUsers: ObservableList<FireUser> = ObservableArrayList<FireUser>()
    private val fireStoreDB = FirebaseFirestore.getInstance()
    private var isAddCallActivity = false


    private fun initLastAdapter(): LastAdapter {
        return LastAdapter(listOfRegisteredUsers, BR.item)
                .map<FireUser, ItemContactBinding>(R.layout.item_contact) {
                    onBind { holder ->
                        setTextDrawable(holder.binding.item!!, holder.binding.profilePic)
                        holder.itemView.call_btn.setOnClickListener { callUser(holder.binding.item!!) }
                    }
                    onClick {
                        //TODO show contact details
                    }
                }
    }

    private fun setTextDrawable(user: FireUser, profilePic: AppCompatImageView) {
        val generator = ColorGenerator.MATERIAL
        // generate color based on a key (same key returns the same color), useful for list/grid views
        val randomColor = generator.getColor(user.userId)
        val builder = TextDrawable.builder()
                .beginConfig()
                .bold()
                .endConfig()
                .round()
        val drawable = builder.build(user.name.substring(0, 1), randomColor)
        profilePic.setImageDrawable(drawable)
    }

    private fun callUser(item: FireUser) {
        //Check onAddCallClicked in VideoChatViewActivity for intent.getStringExtra(CHAT_ROOM_KEY))

        if (isAddCallActivity) {
            makeCall(item.userId, item.name, item.getPhoneWithCountryCode(), intent.getStringExtra(CHAT_ROOM_KEY))
            return
        }
        if (isCallOnGoing)
            parent_container.snack("End the current call and try again")
        else
            makeCall(item.userId, item.name, item.getPhoneWithCountryCode(), intent.getStringExtra(CHAT_ROOM_KEY))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        ContactsPullTask(WeakReference(this)).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, getLocalCountryCode())
        contact_list.adapter = contactsAdapter

        if (intent != null && intent.action != null)
            isAddCallActivity = intent.action == ADD_PEOPLE_ACTION

        if (!isAddCallActivity) {
            val welcomeMessage = "${getWelcomeMessage()}, ${UserInfo.name}"
            welcome_tv.text = welcomeMessage
        } else {
            initAddCallView()
        }
    }

    private fun initAddCallView() {
        welcome_tv.text = "Add people to the call"
    }

    private fun fetchContactsFromServer() {
        progressBar.show()
        val docRef = fireStoreDB.collection("users")
        docRef.get().addOnSuccessListener({ documents ->
            progressBar.hide()
            if (!documents.isEmpty) {
                compareAndMergeUsers(documents.toObjects(FireUser::class.java))
            } else {
                Timber.d("fetchContactsFromServer documents isEmpty")
            }
        })
    }

    private fun compareAndMergeUsers(fireList: MutableList<FireUser>) {
        if (listOfLocalUsers != null) {
            val mergeResult = fireList.filter { fireUser ->
                listOfLocalUsers!!.firstOrNull { fireUser.phone == it.mobileNumber } != null
            }.filterNot { it.userId == UserInfo.userId }

            listOfRegisteredUsers.clear()
            listOfRegisteredUsers.addAll(mergeResult)
        } else {
            Timber.d("listOfLocalUsers is NULL")
        }
    }

    override fun onContactPulled(list: MutableList<ContactModel>) {
        listOfLocalUsers = list.distinctBy { it.mobileNumber }
        fetchContactsFromServer()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!isAddCallActivity) menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_logout -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        UserInfo.clear()
        startActivity(Intent(this, FirebasePhoneNumAuthActivity::class.java))
        finish()
    }

    //https://stackoverflow.com/a/17266260/2102794
    private fun getLocalCountryCode(): String {
        val manager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (manager.simCountryIso != null) manager.simCountryIso.toUpperCase().trim() else "ZZ"
    }

    override fun onCallEnded() {
        on_call_tv.visibility = if (isCallOnGoing) View.VISIBLE else View.GONE
    }

    override fun onRtcServiceConnected(rtcEngine: RtcEngine?) {
        if (!isAddCallActivity) {
            on_call_tv.setOnClickListener { launchVideoChatActivity() }
            on_call_tv.visibility = if (isCallOnGoing) View.VISIBLE else View.GONE
        }
    }

    private fun launchVideoChatActivity() {
        startActivity(Intent(this, VideoChatViewActivity::class.java)
                .apply { putExtra(CHAT_ROOM_KEY, lastKnownRoomID) })
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginFailedEvent(event: LoginFailedEvent) {
        showDialogWithAction("Please check your internet and try again",
                title = "Login failed error ${event.error}",
                okText = "Retry", cancelText = "", onPositiveClick = { retrySignalLogin() })
    }

    @Subscribe
    fun onShowSnackEvent(event: ShowSnackEvent) {
        parent_container.snack(event.message)
    }

    override fun onStart() {
        super.onStart()
        mEventBus.regOnce(this)
    }

    override fun onStop() {
        mEventBus.unregOnce(this)
        super.onStop()
    }

    override fun onBackPressed() {
        if (isAddCallActivity) {
            finish()
            val intent = Intent(this, VideoChatViewActivity::class.java)
            intent.putExtra(CHAT_ROOM_KEY, getIntent().getStringExtra(CHAT_ROOM_KEY))
            startActivity(intent)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        const val ADD_PEOPLE_ACTION = "ADD_PEOPLE_ACTION"
    }

}

class ContactsPullTask(private val weakActivity: WeakReference<Activity>) : AsyncTask<String, Void, MutableList<ContactModel>>() {
    override fun onPreExecute() {
        super.onPreExecute()
        if (weakActivity.get() !is ContactsPullTaskInteractionListener)
            throw RuntimeException("Activity must implement ContactsPullTaskInteractionListener")
    }

    public override fun doInBackground(vararg params: String): MutableList<ContactModel> {
        //IMPORTANT to pass applicationContext
        return ContactsHelper.getAllLocalContacts2(weakActivity.get()?.applicationContext, params[0])
    }

    public override fun onPostExecute(result: MutableList<ContactModel>) {
        val activity = weakActivity.get()
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            // activity is no longer valid, don't do anything!
            Timber.d("onPostExecute error  activity is not available")
            return
        }
        // The activity is still valid, do main-thread stuff here
        if (result.isNotEmpty()) (weakActivity.get() as ContactsPullTaskInteractionListener).onContactPulled(result)
        else Timber.d("onPostExecute error result is empty")
    }

    interface ContactsPullTaskInteractionListener {
        fun onContactPulled(list: MutableList<ContactModel>)
    }

}


