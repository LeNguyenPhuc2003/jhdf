/*******************************************************************************
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 * 
 * http://jhdf.io
 * 
 * Copyright 2019 James Mudd
 * 
 * MIT License see 'LICENSE' file
 ******************************************************************************/
package io.jhdf.dataset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;

public class ChunkedDatasetTest {

	private static final String HDF5_TEST_FILE_NAME = "../test_chunked_datasets_earliest.hdf5";

	private static HdfFile hdfFile;

	@BeforeAll
	static void setup() {
		String testFileUrl = ChunkedDatasetTest.class.getResource(HDF5_TEST_FILE_NAME).getFile();
		hdfFile = new HdfFile(new File(testFileUrl));
	}

	@TestFactory
	Collection<DynamicNode> chunkedDatasetReadTests() {
		// List of all the datasetPaths
		return Arrays.asList(
				dynamicTest("float32", createTest("/float/float32")),
				dynamicTest("float64", createTest("/float/float64")),
				dynamicTest("int8", createTest("/int/int8")),
				dynamicTest("int16", createTest("/int/int16")),
				dynamicTest("int32", createTest("/int/int32")));
	}

	private Executable createTest(String datasetPath) {
		return () -> {
			Dataset dataset = hdfFile.getDatasetByPath(datasetPath);
			Object data = dataset.getData();
			assertThat(getDimensions(data), is(equalTo(new int[] { 7, 5 })));
			Object[] flatData = flatten((Object[]) data);
			for (int i = 0; i < flatData.length; i++) {
				// Do element comparison as there are all different primitive numeric types
				// convert to double
				assertThat(Double.valueOf(flatData[i].toString()), is(equalTo((double) i)));
			}
		};
	}

	private Object[] flatten(Object[] data) {
		List<Object> flat = new ArrayList<>();
		flattenInternal(data, flat);
		return flat.toArray();
	}

	private void flattenInternal(Object data, List<Object> flat) {
		int length = Array.getLength(data);
		for (int i = 0; i < length; i++) {
			Object element = Array.get(data, i);
			if (element.getClass().isArray()) {
				flattenInternal(element, flat);
			} else {
				flat.add(element);
			}
		}
	}

	private int[] getDimensions(Object data) {
		List<Integer> dims = new ArrayList<>();
		dims.add(Array.getLength(data));

		while (Array.get(data, 0).getClass().isArray()) {
			data = Array.get(data, 0);
			dims.add(Array.getLength(data));
		}
		return ArrayUtils.toPrimitive(dims.toArray(new Integer[dims.size()]));
	}
}