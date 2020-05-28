package com.mobymagic.clairediary.ui.createsession

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mobymagic.clairediary.repository.ClaireVartarRepository
import com.mobymagic.clairediary.vo.ClaireVartar
import com.mobymagic.clairediary.vo.Resource

class ClaireVartarViewModel(
        private val claireVartarRepository: ClaireVartarRepository
) : ViewModel() {

    private lateinit var claireVartarLiveData: LiveData<Resource<List<ClaireVartar>>>

    init {
        updateClairevartarsLivedata()
    }

    private fun updateClairevartarsLivedata() {
        claireVartarLiveData = claireVartarRepository.getAllClaireVartar(null)
    }

    fun getClaireVartars(): LiveData<Resource<List<ClaireVartar>>> {
        return claireVartarLiveData
    }

    fun retry() {
        updateClairevartarsLivedata()
    }


}