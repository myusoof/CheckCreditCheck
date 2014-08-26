package uk

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
    StringBuilder stringBuilderHeader = new StringBuilder()
    StringBuilder stringBuilder = new StringBuilder()
    protected JsonFactory jsonFactory = new MappingJsonFactory()
    ObjectMapper objectMapper = new ObjectMapper()
    static MultivaluedMap consumerMap = new MultivaluedHashMap()
    WebTarget target= ClientBuilder.newClient().target("")
    String startDate = new SimpleDateFormat("yyyyMMdd").format(new Date())
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
        stringBuilderHeader.append("InternalOrderId^msisdn^portalId^isPreOrder^journeyType^createdTime^status^devicedescription^deviceSKU^tariffDescription^tariffPID^creditCheckStatusCondition^creditCheckStatus^contactNumber^creditVetNumber^creditScore^lastCreditCheckDate^referralReason")
        stringBuilderHeader.append("\n")
        File file = new File("./orderDetails${new SimpleDateFormat("yyyyMMdd").format(new Date())}.csv")
        FileWriter fileWriter = new FileWriter(file.name,true);
        fileWriter.write(stringBuilderHeader.toString())
        Object response = getRequest(target,consumerMap, "https://prodcat.o2.co.uk/orderService/reports/orders", true)
        String internalOrderId
        String msisdn
        String portalId
        String isPreOrder
        String journeyType
        String createdTime
        String status
        String deviceDescription
        String deviceSKU
        String tariffDescription
        String tariffPID
        String creditCheckStatusCondition
        String creditCheckStatus
        String contactNumber
        String creditVetNumber
        String creditScore
        String lastCreditCheckDate
        String referralReason

        response.each {
            try {
                String orderUrl = it.toString().replaceFirst("http.*8080", "https://prodcat.o2.co.uk")
                println "Currently processing ${orderUrl}"
                Object orderDetail = getRequest(target, consumerMap, orderUrl, false)
                internalOrderId = orderDetail?.id
                msisdn = orderDetail?.msisdn?:null
                portalId = orderDetail?.customerInfo?.portalId
                isPreOrder = orderDetail?.lineItems?.find { it.type == 'device' }?.stock == "PreOrder"
                journeyType = orderDetail?.journeyType
                createdTime = orderDetail?.createdTime
                status = orderDetail?.status
                deviceDescription = orderDetail?.lineItems?.find { it.type == 'device' }?.description
                deviceSKU = orderDetail?.lineItems?.find { it.type == 'device' }?.skuOrPid
                tariffDescription = orderDetail?.lineItems?.find { it.type == "plan" }?.description
                tariffPID = orderDetail?.lineItems?.find { it.type == "plan" }?.skuOrPid
                creditCheckStatusCondition = orderDetail?.requirements?.creditCheckDetails?.met ?: null
                creditCheckStatus = orderDetail.requirements?.creditCheckDetails?.creditCheckStatus ?: null
                contactNumber = orderDetail?.requirements?.creditCheckDetails?.contactNumber ?: null
                creditVetNumber = orderDetail?.requirements?.creditCheckDetails?.creditVetDetails?.creditVetNumber ?: null
                creditScore = orderDetail?.requirements?.creditCheckDetails?.creditVetDetails?.creditScore ?: null
                lastCreditCheckDate = orderDetail?.requirements?.creditCheckDetails?.creditVetDetails?.lastCreditCheckDate ?: null
                referralReason = orderDetail?.requirements?.creditCheckDetails?.creditVetDetails?.referralReason ?: null
            }catch(Exception e){
               println "Not able to process ${internalOrderId}"
            }finally{
                stringBuilder.append("${internalOrderId}^${msisdn}^${portalId}^${isPreOrder}^${journeyType}^${createdTime}^${status}^${deviceDescription}^${deviceSKU}^${tariffDescription}^${tariffPID}^${creditCheckStatusCondition}^${creditCheckStatus}^${contactNumber}^${creditVetNumber}^${creditScore}^${lastCreditCheckDate}^${referralReason}")
                stringBuilder.append("\n")
                fileWriter.write(stringBuilder.toString())
                stringBuilder.setLength(0)
            }

        }


    }

    Object getRequest(WebTarget target,MultivaluedHashMap map, String path, boolean queryParam) throws Exception{
        target = target.path(path)

        if(queryParam){
            println "extracting order status from ${startDate} till ${endDate}"
           target = target.queryParam("startDate", startDate).queryParam("endDate", endDate)
        }
        String response = target.request(MediaType.APPLICATION_JSON_TYPE).headers(map).get(String.class)
        Object jsonNode = objectMapper.readValue(response, Object.class)
        jsonNode
    }
}
