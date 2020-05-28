package com.mobymagic.clairediary.util.filters

import com.mobymagic.clairediary.vo.Session

class TrendingSessionFilter(val userId: String) : ResultFilter<Session> {
    override fun filter(t: List<Session>): List<Session> {
        val filteredSessions: MutableList<Session> = t.toMutableList()
        for (session in t) {
            if (session.followers.contains(userId)) {
                filteredSessions.remove(session)
            }
        }
        return filteredSessions
    }
}