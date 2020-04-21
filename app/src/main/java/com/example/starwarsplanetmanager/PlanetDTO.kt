package com.example.starwarsplanetmanager

class PlanetDTO {
    var IDPlanet: Int = 0
    var Name: String = ""
    var Type: String = ""
    var Government: String = ""
    var RegionID: Int = 0
    var Image: String? = null

    constructor(planet: Planet) {
        this.IDPlanet = planet.IDPlanet
        this.Name = planet.Name
        this.Type = planet.Type
        this.Government = planet.Government
        this.RegionID = planet.RegionID
        this.Image = null

    }
}

class GovernorDTO() {
    var IDPlanet: Int = 0
    var Name: String = ""
    var Surname: String = ""
    var Image: Any? = null

    constructor(planet: Planet):this(){
        this.IDPlanet = planet.IDPlanet
        this.Name = planet.Governor!!.Name
        this.Surname = planet.Governor!!.Surname
        this.Image = planet.Governor!!.Image
        this.Image = null
    }
}

class ArmyTypeDTO(){
    var IDPlanet: Int = 0
    var B1: Int = 0
    var B2: Int = 0
    var B3: Int = 0
    var BX: Int = 0
    var BL: Int = 0
    var D1S1: Int = 0
    var HKB3: Int = 0
    var IG110: Int = 0
    var OG9: Int = 0
    var T4: Int = 0

    constructor(planet: Planet):this(){
        this.IDPlanet = planet.IDPlanet
        this.B1 = planet.ArmyType!!.B1
        this.B2 = planet.ArmyType!!.B2
        this.B3 = planet.ArmyType!!.B3
        this.BX = planet.ArmyType!!.BX
        this.BL = planet.ArmyType!!.BL
        this.D1S1 = planet.ArmyType!!.D1S1
        this.HKB3 = planet.ArmyType!!.HKB3
        this.IG110 = planet.ArmyType!!.IG110
        this.OG9 = planet.ArmyType!!.OG9
        this.T4 = planet.ArmyType!!.T4
    }
}