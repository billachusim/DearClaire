package com.mobymagic.clairediary.ui.sessionlist

enum class SessionListType {
    EGO,
    TRENDING,
    DIARY,
    FOLLOWING,
    NON_ASSIGNED,
    ASSIGNED,
    FLAGGED,
    ALL;

    companion object {

        fun isAlterEgo(sessionListType: SessionListType): Boolean {
            return sessionListType == ASSIGNED || sessionListType == NON_ASSIGNED || sessionListType == ALL
        }

    }

}