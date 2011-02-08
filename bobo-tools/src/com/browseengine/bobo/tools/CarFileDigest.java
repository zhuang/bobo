/**
 * Bobo Browse Engine - High performance faceted/parametric search implementation 
 * that handles various types of semi-structured data.  Written in Java.
 * 
 * Copyright (C) 2005-2006  John Wang
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * To contact the project administrators for the bobo-browse project, 
 * please go to https://sourceforge.net/projects/bobo-browse/, or 
 * send mail to owner@browseengine.com.
 */


/*
 * @author Zhuochuan Huang <zhuang@linkedin.com>
 */

package com.browseengine.bobo.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import com.browseengine.bobo.index.digest.FileDigester;

// used to generate car data index from data file of 15000 cars
public class CarFileDigest extends FileDigester
{
	private static void makeCar(Document car, String carLine, Long id)
	{
		/*
		color:yellow
		year:00000000000000001994
		price:00000000000000007500
		tags:hybrid,leather,moon-roof,reliable
		mileage:00000000000000014900
		category:compact
		makemodel:asian/acura/1.6el
		city:u.s.a./florida/tampa
		uid:00000000000000000001
		*/
		
		String[] parts = carLine.split(";");
		
		String color     = parts[0];
		String year      = parts[1];
		String price     = parts[2];
		String tags      = parts[3];
		String mileage   = parts[4];
		String category  = parts[5];
		String makemodel = parts[6];
		String city      = parts[7];
		
		String uid = String.format("%020d", id);
		
		car.add(new Field("color", color,         Store.NO, Index.NOT_ANALYZED));
		car.add(new Field("year", year,           Store.NO, Index.NOT_ANALYZED));
		car.add(new Field("price", price,         Store.NO, Index.NOT_ANALYZED));
		car.add(new Field("tags", tags,           Store.NO, Index.NOT_ANALYZED));
		car.add(new Field("mileage", mileage,     Store.NO, Index.NOT_ANALYZED));
		car.add(new Field("category", category,   Store.NO, Index.NOT_ANALYZED));
		car.add(new Field("city", city,           Store.NO, Index.NOT_ANALYZED));
		car.add(new Field("makemodel", makemodel, Store.NO, Index.NOT_ANALYZED));
		car.add(new Field("uid", uid,             Store.NO, Index.NOT_ANALYZED));
	}
	
	public CarFileDigest(File file)
	{
		super(file);
	}

	@Override
	public void digest(DataHandler handler) throws IOException
	{
		int numcars = getMaxDocs();
		FileInputStream fin = null;
		try
		{			
			fin = new FileInputStream(getDataFile());
			BufferedReader br = new BufferedReader(new InputStreamReader(fin, getCharset()));
			String line;
			String carLine = "";
			long i = 0;
			while ((line = br.readLine()) != null && i < numcars)
			{
				if ("<EOD>".equals(line))
				{
					Document car = new Document();
					makeCar(car, carLine, ++ i);
					handler.handleDocument(car);
					carLine = "";
				}
				else
				{
					String[] pair = line.split(":");
					if (!"".equals(carLine))
					{
						carLine += ";"; 
					}
					carLine += pair[1];
				}
			}
			br.close();
		}
		catch (IOException e)
		{
			System.err.println("Error: " + e);
		}
		finally
		{
			if (fin != null)
			{
				fin.close();
			}
		}
	}
}
