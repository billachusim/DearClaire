package com.mobymagic.clairediary.ui.chatrooms.data

import com.mobymagic.clairediary.ui.chatrooms.pojo.ChatRoomPojo
import java.util.*

object RoomData {

    val chatRoom: List<ChatRoomPojo>
        get() {

            val chatRoomPojoList = ArrayList<ChatRoomPojo>()

            var chatRoomPojo = ChatRoomPojo("Love and Relationship Garden", "Default", "#88050B", "Hello Darlings,\n" + "For all of us that believe that falling in love and building good relationships is not far from the only reason of our existence; Welcome to the Love and Relationship Chat Room.")
            chatRoomPojoList.add(chatRoomPojo)

            chatRoomPojo = ChatRoomPojo("Entertainment and Comedy Hall", "Default", "#0407ED", "Hello Darlings,\n" + "What's cooking in the streets? Who bit Beyoncé? Which celebrity took everyone's attention yesterday and who is taking it this weekend? But for today, please, who can make us laugh? Welcome to the Entertainment and Comedy Chat Room.")
            chatRoomPojoList.add(chatRoomPojo)

            chatRoomPojo = ChatRoomPojo("The Fashion Walkway", "Default", "#d50000", "Hello Darlings,\n" + "Do you have a date or meeting tomorrow and don't know what to wear? And by \"don't know what to wear?\" I mean maybe you have different shades of sunrise yellow crop jackets and can't decide which of them works for your cobalt blue sneakers or oxblood stilettos or just maybe you don't even have a nice shoe anymore. Welcome to the Fashion Chat Room where we can all help each other slay.")
            chatRoomPojoList.add(chatRoomPojo)

            chatRoomPojo = ChatRoomPojo("Political and Social Issues", "Default", "#3E2723", "Hello Darlings,\n" + "Our generation has been famously tagged as the most politically and socially \"woke\" generation ever. Using the power of tech and social media to stand against bad political leadership and speak up against social inequalities and abuse. Welcome to the Political and Social Issues Chat Room.")
            chatRoomPojoList.add(chatRoomPojo)

            chatRoomPojo = ChatRoomPojo("Sports Analysis Centre", "Default", "#096D0E", "Hello Darlings,\n" + "Share scores, statistics and banter. Rep your favourite teams and players. Analyse the latest games and predict coming games with other fans from all over the world. Welcome to the Sports Analysis Chat Room.")
            chatRoomPojoList.add(chatRoomPojo)

            chatRoomPojo = ChatRoomPojo("Education Classroom", "Default", "#000003", "Hello Darlings,\n" + "This classroom has no teacher. Technically, we are all learners and teachers because to teach you will have to learn and to learn you'll have to teach. Welcome to the Education Chat Room.")
            chatRoomPojoList.add(chatRoomPojo)

            chatRoomPojo = ChatRoomPojo("Health Care Ward", "Default", "#239B56", "Hello Darlings,\n" + "Welcome to the Healthcare Chat Room. Discuss symptoms, diagnosis and share healthertaining information about common sicknesses. If you are sick, we'll wish you well or even sing you \"Soft Kitty\" but remember to visit a doctor because we really love to have you around.")
            chatRoomPojoList.add(chatRoomPojo)

            chatRoomPojo = ChatRoomPojo("Arts and Creativity Studio", "Default", "#880E4F", "Hello Darlings,\n" + "Weirdos, Geeks, Jacks, Queers, Freaks, Worms and Nerds all have something in common; Spending more time doing something no one else can do and only few can understand. Welcome to the Arts and Creativity Chat Room. Share.")
            chatRoomPojoList.add(chatRoomPojo)

            chatRoomPojo = ChatRoomPojo("One On One Chat", "Default", "#4A148C", "Hello Darlings,\n" + "Here, you can start a heart to heart chat with just one unknown and anonymous user just to talk about a topic of mutual interest, offload some burdens or to share anonymous information. DO NOT SHARE PERSONAL INFORMATION")
            chatRoomPojoList.add(chatRoomPojo)

            chatRoomPojo = ChatRoomPojo("Five Aside Chat", "Default", "#0D47A1", "Hello Darlings,\n" + "Here, you can start a small room of closed chat between you and just four other anonymous users online. All chats are gone when you leave the room. DO NOT SHARE PERSONAL INFORMATION")
            chatRoomPojoList.add(chatRoomPojo)

            chatRoomPojo = ChatRoomPojo("Eleven Aside Chat", "Default", "#1B5E20", "Hello Darlings,\n" + "Here, you can start a small room of closed chat between you and ten other anonymous users online. All chats are gone when you leave the room. DO NOT SHARE PERSONAL INFORMATION")
            chatRoomPojoList.add(chatRoomPojo)

            return chatRoomPojoList
        }

}
