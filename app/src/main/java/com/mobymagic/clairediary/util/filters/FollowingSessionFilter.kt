package com.mobymagic.clairediary.util.filters

import com.mobymagic.clairediary.vo.Session

/**
 * A class used to filte session for
 */
class FollowingSessionFilter(val userId: String) : ResultFilter<Session> {
    override fun filter(t: List<Session>): List<Session> {
        val filteredSessions: MutableList<Session> = t.toMutableList()
        for (session in t) {
            if (!session.followers.contains(userId)) {
                filteredSessions.remove(session)
            }
        }
        return filteredSessions
    }

}