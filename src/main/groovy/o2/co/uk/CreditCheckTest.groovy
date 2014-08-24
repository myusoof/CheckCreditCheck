package o2.co.uk

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.MappingJsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test

import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap
import java.text.SimpleDateFormat

import static javax.ws.rs.client.Entity.entity
import static javax.ws.rs.client.Entity.json

/**
 * Created with IntelliJ IDEA.
 * User: yusoof
 * Date: 24/08/14
 * Time: 12:38
 * To change this template use File | Settings | File Templates.
 */
class CreditCheckTest {
    StringBuilder stringBuilder = new StringBuilder()
    protected JsonFactory jsonFactory = new MappingJsonFactory()
    ObjectMapper objectMapper = new ObjectMapper()
    static MultivaluedMap consumerMap = new MultivaluedHashMap()
    WebTarget target= ClientBuilder.newClient().target("")
    String startDate = setDate(2014, 7, 12)
    String endDate = new SimpleDateFormat("yyyyMMdd").format(new Date())

    String setDate(int year, int month, int date){
        Calendar calendar = Calendar.getInstance()
        calendar.set(year, month, date)
        new SimpleDateFormat("yyyyMMdd").format(calendar.getTime())
    }

    @Test
    void creditCheckStatusTest(){
        consumerMap.add("X-TouchPoint", "cfu")
        consumerMap.add("Content-Type", "application/json")
        consumerMap.add("X-IdType", "IdentityUID")
        consumerMap.add("X-Channel", "ConsumerUpgrade")
        consumerMap.add("X-UserId", "ID-000100")
        stringBuilder.append("InternalOrderId, journeyType, createdTime, status, devicedescription, deviceSKU,creditCheckStatusCondition, creditCheckStatus, contactNumber, creditVetNumber, creditScore, lastCreditCheckDate, referralReason")
        stringBuilder.append("\n")
        Object response = getRequest(target,consumerMap, "http://localhost:8080/orderService/v1/reports/orders", true)


        response.each {
            Object orderDetail = getRequest(target, consumerMap, it, false)
            String internalOrderId = orderDetail.id
            String journeyType = orderDetail.journeyType
            String createdTime = orderDetail.createdTime
            String status = orderDetail.status
            String deviceDescription = orderDetail.lineItems.find{it.type == 'device'}.description
            String deviceSKU = orderDetail.lineItems.find{it.type == 'device'}.skuOrPid
            String creditCheckStatusCondition = orderDetail?.requirements?.creditCheckDetails?.met?:null
            String creditCheckStatus = orderDetail.requirements?.creditCheckDetails?.creditCheckStatus?:null
            String contactNumber= orderDetail?.requirements?.creditCheckDetails?.contactNumber?:null
            String creditVetNumber= orderDetail?.requirements?.creditCheckDetails?.creditVetDetails?.creditVetNumber?:null
            String creditScore    = orderDetail?.requirements?.creditCheckDetails?.creditVetDetails?.creditScore?:null
            String lastCreditCheckDate= orderDetail?.requirements?.creditCheckDetails?.creditVetDetails?.lastCreditCheckDate?:null
            String referralReason = orderDetail?.requirements?.creditCheckDetails?.creditVetDetails?.referralReason?:null

            stringBuilder.append("${internalOrderId}, ${journeyType}, ${createdTime}, ${status}, ${deviceDescription}, ${deviceSKU}, ${creditCheckStatusCondition}, ${creditCheckStatus}, ${contactNumber}, ${creditVetNumber}, ${creditScore}, ${lastCreditCheckDate}, ${referralReason}")
            stringBuilder.append("\n")
        }
        File file = new File("./orderDetails${new SimpleDateFormat("yyyyMMdd").format(new Date())}.csv")
        file.setText(stringBuilder.toString())
    }

    Object getRequest(WebTarget target,MultivaluedHashMap map, String path, boolean queryParam){


        target = target.path(path)
        println "extracting order status from ${startDate} till ${endDate}"
        if(queryParam){
           target = target.queryParam("startDate", startDate).queryParam("endDate", endDate)
        }
        String response = target.request(MediaType.APPLICATION_JSON_TYPE).headers(map).get(String.class)
        Object jsonNode = objectMapper.readValue(response, Object.class)
        jsonNode
    }
}
