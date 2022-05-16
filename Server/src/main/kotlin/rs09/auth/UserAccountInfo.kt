package rs09.auth

class UserAccountInfo(
    var username: String,
    var password: String,
    var uid: Int,
    var rights: Int,
    var credits: Int,
    var ip: String,
    var lastUsedIp: String,
    var muteEndTime: Long,
    var banEndTime: Long,
    var contacts: String,
    var blocked: String,
    var clanName: String,
    var currentClan: String,
    var clanReqs: String,
    var timePlayed: Long,
    var lastLogin: Long,
    var online: Boolean
) {
    companion object {
        @JvmStatic fun createDefault() : UserAccountInfo {
            return UserAccountInfo("", "", 0, 0, 0,  "", "", 0L, 0L, "", "", "", "", "1,0,8,9", 0L, 0L, false)
        }
    }

    override fun toString(): String {
        return "USER:$username,PASS:$password,UID:$uid,RIGHTS:$rights,CREDITS:$credits,IP:$ip,LASTIP:$lastUsedIp"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserAccountInfo

        if (username != other.username) return false
        if (password != other.password) return false
        if (uid != other.uid) return false
        if (rights != other.rights) return false
        if (credits != other.credits) return false
        if (ip != other.ip) return false
        if (lastUsedIp != other.lastUsedIp) return false
        if (muteEndTime != other.muteEndTime) return false
        if (banEndTime != other.banEndTime) return false
        if (contacts != other.contacts) return false
        if (blocked != other.blocked) return false
        if (clanName != other.clanName) return false
        if (currentClan != other.currentClan) return false
        if (clanReqs != other.clanReqs) return false
        if (timePlayed != other.timePlayed) return false
        if (lastLogin != other.lastLogin) return false
        if (online != other.online) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + uid
        result = 31 * result + rights
        result = 31 * result + credits
        result = 31 * result + ip.hashCode()
        result = 31 * result + lastUsedIp.hashCode()
        result = 31 * result + muteEndTime.hashCode()
        result = 31 * result + banEndTime.hashCode()
        result = 31 * result + contacts.hashCode()
        result = 31 * result + blocked.hashCode()
        result = 31 * result + clanName.hashCode()
        result = 31 * result + currentClan.hashCode()
        result = 31 * result + clanReqs.hashCode()
        result = 31 * result + timePlayed.hashCode()
        result = 31 * result + lastLogin.hashCode()
        result = 31 * result + online.hashCode()
        return result
    }


}