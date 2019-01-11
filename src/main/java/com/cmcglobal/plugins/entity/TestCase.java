package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import org.springframework.core.annotation.Order;

import java.util.Date;

import static net.java.ao.schema.StringLength.UNLIMITED;

@Preload
public interface TestCase extends Entity {

    @NotNull
    long getIssueId();

    @NotNull
    void setIssueId(long issueId);

    @Order(value = 1)
    @StringLength(value = UNLIMITED)
    String getPhase();

    @StringLength(value = UNLIMITED)
    void setPhase(String phase);

    @Order(value = 2)
    @StringLength(value = UNLIMITED)
    String getTestcaseType();

    @StringLength(value = UNLIMITED)
    void setTestcaseType(String testcaseType);

    @Order(value = 3)
    @StringLength(value = UNLIMITED)
    String getTestcaseNo();

    @StringLength(value = UNLIMITED)
    void setTestcaseNo(String testcaseNo);

    @Order(value = 4)
    @StringLength(value = UNLIMITED)
    String getTestcaseId();

    @StringLength(value = UNLIMITED)
    void setTestcaseId(String testcaseId);

    @Order(value = 5)
    @StringLength(value = UNLIMITED)
    String getTestCategory();

    @StringLength(value = UNLIMITED)
    void setTestCategory(String testCategory);

    @Order(value = 6)
    @StringLength(value = UNLIMITED)
    String getTestViewPoint();

    @StringLength(value = UNLIMITED)
    void setTestViewPoint(String testViewPoint);

    @Order(value = 7)
    @StringLength(value = UNLIMITED)
    String getEvaluationTarget1();

    @StringLength(value = UNLIMITED)
    void setEvaluationTarget1(String evaluationTarget1);

    @Order(value = 8)
    @StringLength(value = UNLIMITED)
    String getEvaluationTarget2();

    @StringLength(value = UNLIMITED)
    void setEvaluationTarget2(String evaluationTarget2);

    @Order(value = 9)
    @StringLength(value = UNLIMITED)
    String getEvaluationTarget3();

    @StringLength(value = UNLIMITED)
    void setEvaluationTarget3(String evaluationTarget3);

    @Order(value = 10)
    @StringLength(value = UNLIMITED)
    String getEvaluationTarget4();

    @StringLength(value = UNLIMITED)
    void setEvaluationTarget4(String evaluationTarget4);

    @Order(value = 11)
    @StringLength(value = UNLIMITED)
    String getEvaluationTarget5();

    @StringLength(value = UNLIMITED)
    void setEvaluationTarget5(String evaluationTarget5);

    @Order(value = 12)
    @StringLength(value = UNLIMITED)
    String getEvaluationTarget6();

    @StringLength(value = UNLIMITED)
    void setEvaluationTarget6(String evaluationTarget6);

    @Order(value = 13)
    @StringLength(value = UNLIMITED)
    String getTestClassification();

    @StringLength(value = UNLIMITED)
    void setTestClassification(String testClassification);

    @Order(value = 14)
    @StringLength(value = UNLIMITED)
    String getAnalysisFlag1();

    @StringLength(value = UNLIMITED)
    void setAnalysisFlag1(String analysisFlag1);

    @Order(value = 15)
    @StringLength(value = UNLIMITED)
    String getAnalysisFlag2();

    @StringLength(value = UNLIMITED)
    void setAnalysisFlag2(String analysisFlag2);

    @Order(value = 16)
    @StringLength(value = UNLIMITED)
    String getPurpose();

    @StringLength(value = UNLIMITED)
    void setPurpose(String purpose);

    @Order(value = 17)
    @StringLength(value = UNLIMITED)
    String getPrecondition();

    @StringLength(value = UNLIMITED)
    void setPrecondition(String precondition);

    @Order(value = 18)
    @StringLength(value = UNLIMITED)
    String getSteps();

    @StringLength(value = UNLIMITED)
    void setSteps(String steps);

    @Order(value = 19)
    @StringLength(value = UNLIMITED)
    String getNumberOfExecutions();

    @StringLength(value = UNLIMITED)
    void setNumberOfExecutions(String numberOfExecutions);

    @Order(value = 20)
    @StringLength(value = UNLIMITED)
    String getConfirmationDisplay();

    @StringLength(value = UNLIMITED)
    void setConfirmationDisplay(String confirmationDisplay);

    @Order(value = 21)
    @StringLength(value = UNLIMITED)
    String getConfirmationAudio();

    @StringLength(value = UNLIMITED)
    void setConfirmationAudio(String confirmationAudio);

    @Order(value = 22)
    @StringLength(value = UNLIMITED)
    String getRemark();

    @StringLength(value = UNLIMITED)
    void setRemark(String remark);

    @Order(value = 23)
    @StringLength(value = UNLIMITED)
    String getPseudoEnvironment();

    @StringLength(value = UNLIMITED)
    void setPseudoEnvironment(String pseudoEnvironment);

    @Order(value = 24)
    @StringLength(value = UNLIMITED)
    String getSystemEnvironment();

    @StringLength(value = UNLIMITED)
    void setSystemEnvironment(String systemEnvironment);

    @Order(value = 25)
    @StringLength(value = UNLIMITED)
    String getUserEnvironment();

    @StringLength(value = UNLIMITED)
    void setUserEnvironment(String userEnvironment);

    @Order(value = 26)
    @StringLength(value = UNLIMITED)
    String getTestData();

    @StringLength(value = UNLIMITED)
    void setTestData(String testData);

    @Order(value = 27)
    @StringLength(value = UNLIMITED)
    String getEvaluationEnvironment();

    @StringLength(value = UNLIMITED)
    void setEvaluationEnvironment(String evaluationEnvironment);

    @Order(value = 28)
    @StringLength(value = UNLIMITED)
    String getEvaluationExecutionPlace();

    @StringLength(value = UNLIMITED)
    void setEvaluationExecutionPlace(String evaluationExecutionPlace);

    @Order(value = 29)
    @StringLength(value = UNLIMITED)
    String getChangeIndicator();

    @StringLength(value = UNLIMITED)
    void setChangeIndicator(String changeIndicator);

    @Order(value = 30)
    @StringLength(value = UNLIMITED)
    String getReasonForChange();

    @StringLength(value = UNLIMITED)
    void setReasonForChange(String reasonForChange);

    @Order(value = 31)
    @StringLength(value = UNLIMITED)
    String getSortItemNumber();

    @StringLength(value = UNLIMITED)
    void setSortItemNumber(String sortItemNumber);

    @Order(value = 32)
    @StringLength(value = UNLIMITED)
    String getReleaseVersionNumber();

    @StringLength(value = UNLIMITED)
    void setReleaseVersionNumber(String releaseVersionNumber);

    @Order(value = 33)
    @StringLength(value = UNLIMITED)
    String getRequestListVersion();

    @StringLength(value = UNLIMITED)
    void setRequestListVersion(String requestListVersion);

    @Order(value = 34)
    @StringLength(value = UNLIMITED)
    String getReimplementationFlag();

    @StringLength(value = UNLIMITED)
    void setReimplementationFlag(String reimplementationFlag);

    @Order(value = 35)
    @StringLength(value = UNLIMITED)
    String getPrecheckFlag();

    @StringLength(value = UNLIMITED)
    void setPrecheckFlag(String precheckFlag);

    @Order(value = 36)
    @StringLength(value = UNLIMITED)
    String getTestCaseVersion();

    @StringLength(value = UNLIMITED)
    void setTestCaseVersion(String testCaseVersion);

    @Order(value = 37)
    @StringLength(value = UNLIMITED)
    String getIncidentId();

    @StringLength(value = UNLIMITED)
    void setIncidentId(String incidentId);

    @Order(value = 38)
    @StringLength(value = UNLIMITED)
    String getGroupingNumber();

    @StringLength(value = UNLIMITED)
    void setGroupingNumber(String groupingNumber);

    @Order(value = 39)
    @StringLength(value = UNLIMITED)
    String getTestPhase();

    @StringLength(value = UNLIMITED)
    void setTestPhase(String testPhase);

    @Order(value = 40)
    @StringLength(value = UNLIMITED)
    String getReserved3();

    @StringLength(value = UNLIMITED)
    void setReserved3(String reserved3);

    @Order(value = 41)
    @StringLength(value = UNLIMITED)
    String getReserved4();

    @StringLength(value = UNLIMITED)
    void setReserved4(String reserved4);

    @Order(value = 42)
    @StringLength(value = UNLIMITED)
    String getReserved5();

    @StringLength(value = UNLIMITED)
    void setReserved5(String reserved5);

    @Order(value = 43)
    Date getUpdateDate();

    void setUpdateDate(Date updateDate);

    @Order(value = 44)
    @StringLength(value = UNLIMITED)
    String getCreatorCompany();

    @StringLength(value = UNLIMITED)
    void setCreatorCompany(String creatorCompany);

    @Order(value = 45)
    @StringLength(value = UNLIMITED)
    String getCreatorPerson();

    @StringLength(value = UNLIMITED)
    void setCreatorPerson(String creatorPerson);

    @Order(value = 46)
    @StringLength(value = UNLIMITED)
    String getImportRequest();

    @StringLength(value = UNLIMITED)
    void setImportRequest(String importRequest);

    @Order(value = 47)
    @StringLength(value = UNLIMITED)
    String getFree1();

    @StringLength(value = UNLIMITED)
    void setFree1(String free1);

    @Order(value = 48)
    @StringLength(value = UNLIMITED)
    String getFree2();

    @StringLength(value = UNLIMITED)
    void setFree2(String free2);

    @Order(value = 49)
    @StringLength(value = UNLIMITED)
    String getFree3();

    @StringLength(value = UNLIMITED)
    void setFree3(String free3);

    @Order(value = 50)
    @StringLength(value = UNLIMITED)
    String getFree4();

    @StringLength(value = UNLIMITED)
    void setFree4(String free4);

    @Order(value = 51)
    @StringLength(value = UNLIMITED)
    String getFree5();

    @StringLength(value = UNLIMITED)
    void setFree5(String free5);

    @Order(value = 52)
    @StringLength(value = UNLIMITED)
    String getDestination();

    @StringLength(value = UNLIMITED)
    void setDestination(String destination);

    @Order(value = 53)
    @StringLength(value = UNLIMITED)
    String getHandle();

    @StringLength(value = UNLIMITED)
    void setHandle(String handle);

    @Order(value = 54)
    @StringLength(value = UNLIMITED)
    String getMotorModel();

    @StringLength(value = UNLIMITED)
    void setMotorModel(String motorModel);

    @Order(value = 55)
    @StringLength(value = UNLIMITED)
    String getAudio();

    @StringLength(value = UNLIMITED)
    void setAudio(String audio);

    @Order(value = 56)
    @StringLength(value = UNLIMITED)
    String getBandType();

    @StringLength(value = UNLIMITED)
    void setBandType(String bandType);

    @Order(value = 57)
    @StringLength(value = UNLIMITED)
    String getRadioDataSystem();

    @StringLength(value = UNLIMITED)
    void setRadioDataSystem(String radioDataSystem);

    @Order(value = 58)
    @StringLength(value = UNLIMITED)
    String getXmRadio();

    @StringLength(value = UNLIMITED)
    void setXmRadio(String xmRadio);

    @Order(value = 59)
    @StringLength(value = UNLIMITED)
    String getDigitalAudioBroadcasting();

    @StringLength(value = UNLIMITED)
    void setDigitalAudioBroadcasting(String digitalAudioBroadcasting);

    @Order(value = 60)
    @StringLength(value = UNLIMITED)
    String getTrafficInformation();

    @StringLength(value = UNLIMITED)
    void setTrafficInformation(String trafficInformation);

    @Order(value = 61)
    @StringLength(value = UNLIMITED)
    String getHdRadio();

    @StringLength(value = UNLIMITED)
    void setHdRadio(String hdRadio);

    @Order(value = 62)
    @StringLength(value = UNLIMITED)
    String getTv();

    @StringLength(value = UNLIMITED)
    void setTv(String tv);

    @Order(value = 63)
    @StringLength(value = UNLIMITED)
    String getNavigationType();

    @StringLength(value = UNLIMITED)
    void setNavigationType(String navigationType);

    @Order(value = 64)
    @StringLength(value = UNLIMITED)
    String getLanguage();

    @StringLength(value = UNLIMITED)
    void setLanguage(String language);

    @Order(value = 65)
    @StringLength(value = UNLIMITED)
    String getVoiceRecognition();

    @StringLength(value = UNLIMITED)
    void setVoiceRecognition(String voiceRecognition);

    @Order(value = 66)
    @StringLength(value = UNLIMITED)
    String getAntiTheft();

    @StringLength(value = UNLIMITED)
    void setAntiTheft(String antiTheft);

    @Order(value = 67)
    @StringLength(value = UNLIMITED)
    String getCameraRear();

    @StringLength(value = UNLIMITED)
    void setCameraRear(String cameraRear);

    @Order(value = 68)
    @StringLength(value = UNLIMITED)
    String getLanewatch();

    @StringLength(value = UNLIMITED)
    void setLanewatch(String lanewatch);

    @Order(value = 69)
    @StringLength(value = UNLIMITED)
    String getMultiViewCameraSystem();

    @StringLength(value = UNLIMITED)
    void setMultiViewCameraSystem(String multiViewCameraSystem);

    @Order(value = 70)
    @StringLength(value = UNLIMITED)
    String getCameraMonitorMirrorSystem();

    @StringLength(value = UNLIMITED)
    void setCameraMonitorMirrorSystem(String cameraMonitorMirrorSystem);

    @Order(value = 71)
    @StringLength(value = UNLIMITED)
    String getUsbJack();

    @StringLength(value = UNLIMITED)
    void setUsbJack(String usbJack);

    @Order(value = 72)
    @StringLength(value = UNLIMITED)
    String getHdmiJack();

    @StringLength(value = UNLIMITED)
    void setHdmiJack(String hdmiJack);

    @Order(value = 73)
    @StringLength(value = UNLIMITED)
    String getAuxJack();

    @StringLength(value = UNLIMITED)
    void setAuxJack(String auxJack);

    @Order(value = 74)
    @StringLength(value = UNLIMITED)
    String getCarplay();

    @StringLength(value = UNLIMITED)
    void setCarplay(String carplay);

    @Order(value = 75)
    @StringLength(value = UNLIMITED)
    String getAndroidAuto();

    @StringLength(value = UNLIMITED)
    void setAndroidAuto(String androidAuto);

    @Order(value = 76)
    @StringLength(value = UNLIMITED)
    String getMirrorLink();

    @StringLength(value = UNLIMITED)
    void setMirrorLink(String mirrorLink);

    @Order(value = 77)
    @StringLength(value = UNLIMITED)
    String getTelematics();

    @StringLength(value = UNLIMITED)
    void setTelematics(String telematics);

    @Order(value = 78)
    @StringLength(value = UNLIMITED)
    String getEmergencyCall();

    @StringLength(value = UNLIMITED)
    void setEmergencyCall(String emergencyCall);

    @Order(value = 79)
    @StringLength(value = UNLIMITED)
    String getSoundSystem();

    @StringLength(value = UNLIMITED)
    void setSoundSystem(String soundSystem);

    @Order(value = 80)
    @StringLength(value = UNLIMITED)
    String getPlant();

    @StringLength(value = UNLIMITED)
    void setPlant(String plant);

    @Order(value = 81)
    @StringLength(value = UNLIMITED)
    String getVariation30();

    @StringLength(value = UNLIMITED)
    void setVariation30(String variation30);

    @Order(value = 82)
    Date getOriginalPlanDate();

    void setOriginalPlanDate(Date originalPlanDate);

    @Order(value = 83)
    Date getCurrentPlanDate();

    void setCurrentPlanDate(Date currentPlanDate);

}
