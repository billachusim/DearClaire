package com.mobymagic.clairediary.util

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.*
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.vo.Resource
import timber.log.Timber

class FirestoreSingleLiveData<T>(
    private val androidUtil: AndroidUtil,
    private val query: Query,
    private val type: Class<T>
) : LiveData<Resource<T>>(), EventListener<QuerySnapshot> {

    private var registration: ListenerRegistration? = null

    override fun onEvent(snapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
        Timber.d("FirestoreListLiveData onEvent(%s, %s)", snapshots, e?.toString())
        if (e != null) {
            Timber.e(e)
            value = Resource.error(null)
            return
        }

        value = Resource.success(getDocument(snapshots!!))
    }

    override fun onActive() {
        super.onActive()
        Timber.d("onActive")
        value = Resource.loading(androidUtil.getString(R.string.common_message_loading))
        registration = query.addSnapshotListener(this)
    }

    override fun onInactive() {
        super.onInactive()
        Timber.d("onInactive")
        if (registration != null) {
            registration!!.remove()
            registration = null
        }
    }

    private fun getDocument(snapshots: QuerySnapshot): T? {
        if (!snapshots.documents.isEmpty()) {
            return snapshots.documents[0].toObject(type)
        }

        return null
    }

}