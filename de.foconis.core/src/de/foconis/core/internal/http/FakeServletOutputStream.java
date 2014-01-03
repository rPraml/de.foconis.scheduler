/*
 * © Copyright FOCONIS AG, 2014
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package de.foconis.core.internal.http;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

/**
 * A Wrapper for the servlet output stream
 * 
 * @author praml
 * 
 */
public class FakeServletOutputStream extends ServletOutputStream {

	private OutputStream delegate;

	/**
	 * @param delegate
	 */
	public FakeServletOutputStream(final OutputStream delegate) {
		super();
		this.delegate = delegate;
	}

	/**
	 * @param paramInt
	 * @throws IOException
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(final int paramInt) throws IOException {
		delegate.write(paramInt);
	}

	/**
	 * @param paramArrayOfByte
	 * @throws IOException
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write(final byte[] paramArrayOfByte) throws IOException {
		delegate.write(paramArrayOfByte);
	}

	/**
	 * @param paramArrayOfByte
	 * @param paramInt1
	 * @param paramInt2
	 * @throws IOException
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(final byte[] paramArrayOfByte, final int paramInt1, final int paramInt2) throws IOException {
		delegate.write(paramArrayOfByte, paramInt1, paramInt2);
	}

	/**
	 * @throws IOException
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	/**
	 * @throws IOException
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() throws IOException {
		delegate.close();
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return delegate.toString();
	}

}
