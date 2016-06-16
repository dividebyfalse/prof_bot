import scala.io._
import scala.collection.mutable.ArrayBuffer
import org.json4s._
import org.json4s.native.JsonMethods._

object ProfBot extends App {
  val access_token = "access_token=228e1561f8bfbc97b7b5ebf5b94baadcb94c9d7491e96ac7ccf273fce3dc99030d2263349f29db3fa0406"
  def group_list = getGroupList
  // val url_repost = "https://api.vk.com/method/wall.repost?object=wall-22122847_25237&group_id=12998578&access_token=d14f404d51d4960b2032fabd99fc3d6f271751dd0a0fdfc1b27d2fb96b2285009d2839600f8d02b36ea59"
  // print(Source.fromURL(createUrlRepost("wall-22122847_25237")).mkString)
  def getGroupList = {
    val group_list = new ArrayBuffer[Int]()
    val groups_resp = parse(vk_req("https://api.vk.com/method/groups.get?filter=moder&"+ "v=5.52&"+ access_token)) \ "response"
    val groups_items = groups_resp \ "items"
    val groups_count = (groups_resp \ "count").values.toString.toInt
    for(i <- 0 until groups_count) {
      group_list += groups_items(i).values.toString.toInt
    }
    group_list
  }

  while(true) {
    println(group_list)
    Thread.sleep(1500)
    CheckUpdate
  }
  def CheckUpdate = {
    val dialogs = GetDialogs
    val d_count = dialogs(0).values.toString.toInt
    var i:Int = 0
    while(i < d_count) {
      i = i + 1
      if ((dialogs(i) \ "read_state").values == 0 && (dialogs(i) \ "out").values == 0){
        MarkAsRead(dialogs(i) \ "mid")
        if (dialogs(i) \ "attachment" != JNothing && (dialogs(i) \ "attachment" \ "type").values.toString == "wall"){
          for(elem <- group_list) {
            val group_from_id = (dialogs(i) \ "attachment" \ "wall" \ "from_id").values.toString.toInt
            if(group_list(0) != group_from_id){
              just_do_it(dialogs(i) \ "attachment", elem)
            }
          }
        } else {
          sendMessage("Держитесь_там!_Всего_доброго_и_хорошего_настроения!"+(dialogs(i) \ "mid").values.toString,(dialogs(i) \ "uid").values.toString.toInt)
        }
      }
    }
  }

  def sendMessage(message:String, to_id: Int):String = {
    vk_req("https://api.vk.com/method/messages.send?message="+ message + "&user_id="+ to_id + "&" + access_token)
  }

  def MarkAsRead(message_id: JValue) = {
    val id = message_id.values
    vk_req("https://api.vk.com/method/messages.markAsRead?message_ids="+ id + "&" + access_token)
  }
  def GetDialogs = {
    val resp = vk_req("https://api.vk.com/method/messages.getDialogs?" +access_token)
    parse(resp) \ "response"
  }

  def just_do_it(resp_attach: JValue, group_id: Int):String = {
    vk_req(createUrlRepost(createWallObject(resp_attach)+"&group_id="+ group_id))
  }

  def createWallObject(readstate: JValue):String = {
    val w_id = (readstate \ "wall" \ "id").values.toString.toInt
    val w_group = (readstate \ "wall" \ "from_id").values.toString.toInt
    "wall"+ w_group + "_" + w_id
  }

  def createUrlRepost(wall_object: String):String = {
    "https://api.vk.com/method/wall.repost?object=" + wall_object + "&"+ access_token
  }

  def vk_req(req: String): String = {
    Thread.sleep(500)
    Source.fromURL(req).mkString
  }

}
