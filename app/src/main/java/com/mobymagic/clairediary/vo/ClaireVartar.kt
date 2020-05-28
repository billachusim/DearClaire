package com.mobymagic.clairediary.vo

class ClaireVartar(@field:JvmField
                   var imageUrl: String,
                   @field:JvmField
                   var name: String,
                   @field:JvmField
                   var claireVartarId: String) {

    constructor() : this(
            "",
            "",
            ""
    )
}