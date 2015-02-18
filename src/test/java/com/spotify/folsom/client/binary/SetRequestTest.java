/*
 * Copyright (c) 2014-2015 Spotify AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.spotify.folsom.client.binary;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.spotify.folsom.client.MemcacheEncoder;
import com.spotify.folsom.client.OpCode;

import org.junit.Test;

import java.util.List;

import io.netty.buffer.ByteBuf;


public class SetRequestTest extends RequestTestTemplate {
  private static final String KEY = "foo";
  private static final String VALUE = "val";

  @Test
  public void testBufferNoCas() throws Exception {
    verifySetRequest(0);
  }

  @Test
  public void testBufferCas() throws Exception {
    verifySetRequest(258);
  }

  private void verifySetRequest(long cas) throws Exception {
    SetRequest req = new SetRequest(
      OpCode.ADD,
      KEY,
      Charsets.UTF_8,
      TRANSCODER.encode(VALUE),
      1000,
      cas,
      OPAQUE);

    MemcacheEncoder memcacheEncoder = new MemcacheEncoder();
    List<Object> out = Lists.newArrayList();
    memcacheEncoder.encode(ctx, req, out);
    ByteBuf b = (ByteBuf) out.get(0);

    assertHeader(b, OpCode.ADD, KEY.length(), 8, KEY.length() + 8 + VALUE.length(), req.getOpaque(), cas);
    assertZeros(b, 4);
    assertExpiration(b.readInt());
    assertString(KEY, b);
    assertString(VALUE, b);
    assertEOM(b);
  }
}
