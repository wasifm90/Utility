package com.r4cloud.opportunitygearbox.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.r4cloud.exception.LogicError;


public class FeildValidatorUtil {

	private static final Log log = LogFactory.getLog(FeildValidatorUtil.class);

	public void validate(Object data) {
		String path = "";
		validateEachField(data, path);
	}

	private void validateEachField(Object data, String path) {
		if (data == null) {
			return;
		}
		if(!filterPackage(data.getClass())) {
			return;
		}
		if ((data.getClass().isEnum())) {
			return;
		}


		List<Field> fields = new ArrayList<>();
		fields = getAllFields(fields, data.getClass());
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				if (!field.isSynthetic()) {
					if (field.isAnnotationPresent(Mandatory.class) && field.get(data) == null) {
						throw new LogicError(GearBoxErrorCode.ERROR_IN_REQUEST, path + "/" + field.getName());
					}
					if (Iterable.class.isAssignableFrom(field.getType())) {
						Iterable<Object> iterableObject = (Iterable<Object>) field.get(data);
						if(iterableObject != null) {
							if (field.isAnnotationPresent(Mandatory.class) && iterableObject.iterator().hasNext() == false) {
								throw new LogicError(GearBoxErrorCode.ERROR_IN_REQUEST, path + "/" + field.getName());
							}
							for (Object object : iterableObject) {
								validateEachField(object, path + "/" + field.getName());
							}
						}
					} else {
						validateEachField(field.get(data), path + "/" + field.getName());
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				log.error(e);
			}
		}
	}

	private boolean filterPackage(Class<?> type) {
		return type.getPackage().getName().startsWith("com.r4cloud");
	}

	private List<Field> getAllFields(List<Field> fields, Class<?> type) {
		if(filterPackage(type)) {	
			fields.addAll(Arrays.asList(type.getDeclaredFields()));

			if (type.getSuperclass() != null) {
				getAllFields(fields, type.getSuperclass());
			}
		}
		return fields;
	}

}


