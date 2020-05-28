package com.mobymagic.clairediary.util

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.util.filters.ResultFilter
import com.mobymagic.clairediary.vo.Resource
import timber.log.Timber
import java.util.*

class FirestoreListLiveData<T>(
        private val androidUtil: AndroidUtil,
        private val query: Query,
        private val type: Class<T>,
        private val filter: ResultFilter<T>?
) : LiveData<Resource<List<T>>>(), EventListener<QuerySnapshot> {

    private var registration: ListenerRegistration? = null

    override fun onEvent(snapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
        Timber.d("FirestoreListLiveData onEvent(%s, %s)", snapshots, e?.toString())
        if (e != null) {
            Timber.e(e)
            value = Resource.error(null)
            return
        }

        value = Resource.success(documentToList(snapshots!!))
    }

    override fun onActive() {
        super.onActive()
        Timber.d("onActive")
        if (registration == null) {
            Timber.d("Firebase registration is null")
            value = Resource.loading(androidUtil.getString(R.string.common_message_loading))
            registration = query.addSnapshotListener(this)
        }
    }

    override fun onInactive() {
        super.onInactive()
        Timber.d("onInactive")
        if (!hasObservers()) {
            Timber.d("LiveData is inactive and doesn't have any observers")
            registration?.remove()
            registration = null
        }
    }

    private fun documentToList(snapshots: QuerySnapshot): List<T> {
        val dataList = ArrayList<T>()

        for (document in snapshots.documents) {
            dataList.add(document.toObject(type)!!)
        }
        if (filter != null) {
            return filter.filter(dataList)
        }
        return dataList
    }

}