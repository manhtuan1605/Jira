package com.cmcglobal.plugins.utils;

import com.cmcglobal.plugins.dto.CustomFieldValueDTO;
import com.cmcglobal.plugins.entity.AutomotiveDevice;
import com.cmcglobal.plugins.entity.Defect;
import com.cmcglobal.plugins.entity.ExternalDeviceID;
import com.cmcglobal.plugins.entity.HuDevice;
import com.cmcglobal.plugins.entity.HuTypeVehicle;
import com.cmcglobal.plugins.entity.PeripheralDevice;
import com.cmcglobal.plugins.entity.QnA;
import com.cmcglobal.plugins.entity.ScreenDevice;
import com.cmcglobal.plugins.entity.SetOfDevice;
import com.cmcglobal.plugins.entity.SiAmigoInfo;
import com.cmcglobal.plugins.entity.SiSysInfo;
import com.cmcglobal.plugins.entity.Task;
import com.cmcglobal.plugins.entity.TestCase;
import com.cmcglobal.plugins.entity.TestResult;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.cmcglobal.plugins.utils.Constants.ERR_EXIST_TESTCASE_NO;
import static com.cmcglobal.plugins.utils.Constants.ERR_INVALID_TESTCASE_NO;

public class Utilities {

    private static final Logger logger = LoggerFactory.getLogger(Utilities.class);

    private Utilities() {
    }

    public static List<String> listTestcaseAttribute() {
        return getMethods(TestCase.class);
    }

    public static List<String> listTestResultAttribute() {
        return replaceAttribute(getMethods(TestResult.class), ImmutableMap.of("Rtc Code", "RTC Code"));
    }

    public static List<String> listQnAAttribute() {
        return getMethods(QnA.class);
    }

    public static List<String> listDefectAttribute() {
        return getMethods(Defect.class);
    }

    public static List<String> listHuDeviceAttribute() {
        return getMethods(HuDevice.class);
    }

    public static List<String> listScreenDeviceAttribute() {
        return getMethods(ScreenDevice.class);
    }

    public static List<String> listHuTypeVehicleAttribute() {
        return getMethods(HuTypeVehicle.class);
    }

    public static List<String> listSiSysInfoAttribute() {
        return getMethods(SiSysInfo.class);
    }

    public static List<String> listSiAmigoInfoAttribute() {
        return getMethods(SiAmigoInfo.class);
    }

    public static List<String> listSetOfDeviceAttribute() {
        return getMethods(SetOfDevice.class);
    }

    public static List<String> listAutomotiveDevice() {
        return replaceAttribute(getMethods(AutomotiveDevice.class), ImmutableMap.of("Cmc Code", "CMC Code"));
    }

    public static String makeRTCCodeNormal(String key) {
        Map<String,String> map = new HashMap();
        map.put("R TC Code","RTC Code");
        String value = map.get(key);
        if (StringUtils.isEmpty(value)) return key;
        else return map.get(key);
    }

    public static List<String> listPeripheralDevice() {
        return replaceAttribute(getMethods(PeripheralDevice.class),
                                ImmutableMap.<String, String>builder().put("Cmc Code", "CMC Code")
                                                                      .put("Os", "OS")
                                                                      .put("Os Version", "OS Version")
                                                                      .put("Hfp Version", "HFP Version")
                                                                      .put("Avrcp Version", "AVRCP Version")
                                                                      .put("A 2dp Version", "A2DP Version")
                                                                      .put("Map Version", "MAP Version")
                                                                      .put("Pbap Version", "PBAP Version")
                                                                      .put("At Command", "AT Command")
                                                                      .build());
    }

    public static List<String> listTaskAttribute() {
        return getMethods(Task.class);
    }

    public static List<String> listExternalDevice() {
        return replaceAttribute(getMethods(ExternalDeviceID.class),
                                ImmutableMap.of("U SB 1", "USB 1", "U SB 2", "USB 2", "A UX", "AUX", "H DM I", "HDMI",
                                                "N FC", "NFC"));
    }

    private static String addWhiteSpace(final String str) {
        return str.replaceAll("(.)([A-Z0-9])", "$1 $2");
    }

    private static List<String> getMethods(final Class clazz) {
        final List<String> listMethods = new ArrayList<>();
        final Method[] methods = clazz.getDeclaredMethods();
        Arrays.sort(methods, (Method o1, Method o2) -> {
            final Order or1 = o1.getAnnotation(Order.class);
            final Order or2 = o2.getAnnotation(Order.class);
            // nulls last
            if (or1 != null && or2 != null) {
                return or1.value() - or2.value();
            } else if (or1 != null) {
                return -1;
            } else if (or2 != null) {
                return 1;
            }
            return o1.getName().compareTo(o2.getName());
        });
        for (final Method method : methods) {
            if (method.getAnnotation(Order.class) != null) {
                final String attribute = Utilities.addWhiteSpace(method.getName().substring(3));
                if (!listMethods.contains(attribute)) {
                    listMethods.add(attribute);
                }
            }
        }

        return listMethods;
    }

    public static List<CustomFieldValueDTO> getFieldNamesAndValues(final Object obj, final boolean publicOnly,
                                                                   final Map<String, String> map) {
        final Class c1azz = obj.getClass();
        final List<CustomFieldValueDTO> lst = new ArrayList<>();
        CustomFieldValueDTO customFieldValueDTO;
        final Field[] fields = c1azz.getDeclaredFields();
        for (Field field : fields) {
            final String name = field.getName();
            try {
                if (publicOnly) {
                    if (Modifier.isPublic(field.getModifiers())) {
                        Object value = field.get(obj);
                        customFieldValueDTO = new CustomFieldValueDTO();
                        String attribute = Utilities.addWhiteSpace(name);
                        attribute = makeRTCCodeNormal(attribute);// fix RTC Code name issue
                        customFieldValueDTO.setKey(map != null ? map.getOrDefault(attribute, attribute) : attribute);
                        customFieldValueDTO.setValue(value != null ? value.toString() : "");
                        lst.add(customFieldValueDTO);
                    }
                } else {
                    field.setAccessible(true);
                    final Object value = field.get(obj);
                    customFieldValueDTO = new CustomFieldValueDTO();
                    String attribute = Utilities.addWhiteSpace(name);
                    attribute = makeRTCCodeNormal(attribute);// fix RTC Code name issue
                    customFieldValueDTO.setKey(map != null ? map.getOrDefault(attribute, attribute) : attribute);
                    customFieldValueDTO.setValue(value != null ? value.toString() : "");
                    lst.add(customFieldValueDTO);
                }
            } catch (final IllegalAccessException e) {
                logger.error(e.getMessage());
            }
        }

        return lst;
    }

    public static Map<String, Object> getFieldNamesAndValuesMap(final Object obj, final boolean publicOnly)
            throws IllegalAccessException {
        final Class c1azz = obj.getClass();
        final Map<String, Object> map = new HashMap<>();
        final Field[] fields = c1azz.getDeclaredFields();
        for (Field field : fields) {
            final String name = field.getName();
            if (publicOnly) {
                if (Modifier.isPublic(field.getModifiers())) {
                    final Object value = field.get(obj);
                    map.put(name, value);
                }
            } else {
                field.setAccessible(true);
                final Object value = field.get(obj);
                map.put(name, value);
            }
        }

        return map;
    }

    public static String isValidData(Set<String> dataSet, String data) {
        if (StringUtils.isBlank(data)) {
            return ERR_INVALID_TESTCASE_NO;
        }
        if (dataSet.contains(data)) {
            return ERR_EXIST_TESTCASE_NO;
        }

        return null;
    }

    public static boolean isValidData(final String data, final int maxLength) {
        return !StringUtils.isBlank(data) && data.getBytes(StandardCharsets.UTF_8).length < maxLength;
    }

    private static List<String> replaceAttribute(List<String> list, Map<String, String> map) {
        map.forEach((key, value) -> {
            if (list.contains(key)) {
                list.set(list.indexOf(key), value);
            }
        });

        return list;
    }

}
