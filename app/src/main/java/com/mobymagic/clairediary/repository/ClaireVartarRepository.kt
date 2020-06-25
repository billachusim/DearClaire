package com.mobymagic.clairediary.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.util.FirestoreListLiveData
import com.mobymagic.clairediary.util.FirestoreSingleLiveData
import com.mobymagic.clairediary.vo.AsyncRequest
import com.mobymagic.clairediary.vo.ClaireVartar
import com.mobymagic.clairediary.vo.Resource
import timber.log.Timber

const val COLLECTION_CLAIRE_VARTAR = "claire_vartar"
const val LIMIT_CLAIRE_VARTAR_RESULT = 200L

class ClaireVartarRepository(
        private val appExecutors: AppExecutors,
        private val androidUtil: AndroidUtil,
        private val firestore: FirebaseFirestore
) {

    fun getClaireVartarWithId(claireVartarId: String): LiveData<Resource<ClaireVartar>> {
        Timber.d("Getting clairevartar with id: %s", claireVartarId)
        val query = firestore.collection(COLLECTION_CLAIRE_VARTAR)
                .whereEqualTo("sessionId", claireVartarId)
                .limit(1)

        return FirestoreSingleLiveData(androidUtil, query, ClaireVartar::class.java)
    }


    fun getAllClaireVartar(lastClaireVartar: ClaireVartar?): LiveData<Resource<List<ClaireVartar>>> {
        Timber.d("Getting all clairevartar. Last session: %s", lastClaireVartar)
        val query = firestore.collection(COLLECTION_CLAIRE_VARTAR)
                .orderBy("name", Query.Direction.DESCENDING)
                .limit(LIMIT_CLAIRE_VARTAR_RESULT)

        if (lastClaireVartar != null) query.startAfter(lastClaireVartar.name)

        return FirestoreListLiveData(androidUtil, query, ClaireVartar::class.java, null)
    }

    fun addClaireVartar(claireVartar: ClaireVartar): LiveData<Resource<ClaireVartar>> {
        Timber.d("Adding new claireVartar: %s", claireVartar)
        val addClairevartarRequestLiveData = MutableLiveData<Resource<ClaireVartar>>()

        // Set resource into loading state
        addClairevartarRequestLiveData.value = Resource.loading(
                androidUtil
                        .getString(R.string.new_claire_vartar_saving)
        )

        // Set the session document id
        val newClaireVartarRef = firestore.collection(COLLECTION_CLAIRE_VARTAR).document()
        claireVartar.claireVartarId = newClaireVartarRef.id

        // Save session into the Firestore database
        newClaireVartarRef.set(claireVartar)
                .addOnSuccessListener {
                    Timber.d("ClaireVartar successfully added")
                    addClairevartarRequestLiveData.value = Resource.success(claireVartar)
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error saving new claireVartar")
                    addClairevartarRequestLiveData.value = Resource.error(
                            androidUtil
                                    .getString(R.string.new_claire_vartar_save_error)
                    )
                }

        return addClairevartarRequestLiveData
    }

    fun updateClaireVartar(claireVartar: ClaireVartar): LiveData<Resource<ClaireVartar>> {
        Timber.d("Updating claireVartar: %s", claireVartar)
        val updateClaireVartarRequestLiveData = MutableLiveData<Resource<ClaireVartar>>()

        // Set resource into loading state
        updateClaireVartarRequestLiveData.value = Resource.loading(
                androidUtil
                        .getString(R.string.claire_vartar_updating)
        )

        // Get a reference to the session to update
        val claireVartarToUpdateRef =
                firestore.collection(COLLECTION_CLAIRE_VARTAR).document(claireVartar.claireVartarId)

        // Update the session by merging changes
        claireVartarToUpdateRef.set(claireVartar, SetOptions.merge())
                .addOnSuccessListener { documentReference ->
                    Timber.d("ClaireVartar successfully updated: %s", documentReference)
                    updateClaireVartarRequestLiveData.value = Resource.success(claireVartar)
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error updating claireVartar")
                    updateClaireVartarRequestLiveData.value = Resource.error(
                            androidUtil
                                    .getString(R.string.claire_vartar_update_error)
                    )
                }

        return updateClaireVartarRequestLiveData
    }


    fun deleteClaireVartar(claireVartar: ClaireVartar) {
        Timber.d("Deleting claireVartar: %s", claireVartar)
        val deleteClaireVartarRequestLiveData = MutableLiveData<Resource<AsyncRequest>>()

        // Set resource into loading state
        deleteClaireVartarRequestLiveData.value = Resource.loading(
                androidUtil
                        .getString(R.string.claire_vartar_deleting)
        )

        // Get a reference to the session to delete
        val claireVartarToDeleteRef =
                firestore.collection(COLLECTION_CLAIRE_VARTAR).document(claireVartar.claireVartarId)

        // Delete the session from the database
        claireVartarToDeleteRef.delete()
                .addOnSuccessListener { documentReference ->
                    Timber.d("ClaireVartar successfully deleted: %s", documentReference)
                    deleteClaireVartarRequestLiveData.value = Resource.success(AsyncRequest())
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error deleting claireVartar")
                    deleteClaireVartarRequestLiveData.value = Resource.error(
                            androidUtil
                                    .getString(R.string.claire_vartar_delete_error)
                    )
                }
    }

}