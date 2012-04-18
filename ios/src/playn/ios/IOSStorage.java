/**
 * Copyright 2012 The PlayN Authors
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
package playn.ios;

import cli.System.Environment.SpecialFolder;
import cli.System.Environment;
import cli.System.IO.File;
import cli.System.IO.Path;

import cli.Mono.Data.Sqlite.SqliteConnection;
import cli.System.Data.CommandType;
import cli.System.Data.Common.DbCommand;
import cli.System.Data.Common.DbParameter;
import cli.System.Data.DbType;
import cli.System.Data.IDataReader;

import java.util.ArrayList;
import java.util.List;

import playn.core.PlayN;
import playn.core.Storage;

public class IOSStorage implements Storage
{
  private static final String STORAGE_FILE_NAME = "playn.db";
  private static final String STORAGE_SCHEMA =
    "CREATE TABLE Data (DataKey ntext PRIMARY KEY, DataValue ntext NOT NULL)";

  private SqliteConnection conn;

  public IOSStorage() {
    String docDir = Environment.GetFolderPath(SpecialFolder.wrap(SpecialFolder.Personal));
    String db = Path.Combine(docDir, STORAGE_FILE_NAME);
    boolean needCreate = !File.Exists(db);
    if (needCreate)
      SqliteConnection.CreateFile(db);
    conn = new SqliteConnection("Data Source=" + db);
    if (needCreate)
      executeUpdate(createCommand(STORAGE_SCHEMA));
  }

  @Override
  public boolean isPersisted() {
    return true;
  }

  @Override
  public Iterable<String> keys() {
    try {
      List<String> keys = new ArrayList<String>();
      DbCommand cmd = createCommand("select DataKey from Data");

      IDataReader reader = null;
      try {
        conn.Open();
        reader = cmd.ExecuteReader();
        while (reader.Read()) {
          keys.add(reader.GetString(0));
        }
        return keys;

      } finally {
        if (reader != null)
          reader.Dispose();
        cmd.Dispose();
        conn.Close();
        conn.Dispose();
      }

    } catch (Throwable t) {
      throw new RuntimeException("keys() failed: " + t);
    }
  }

  @Override
  public String getItem(String key) {
    try {
      DbCommand cmd = createCommand("select DataValue from Data where DataKey = @key");
      cmd.get_Parameters().Add(createParam(cmd, "@key", key));

      IDataReader reader = null;
      try {
        conn.Open();
        reader = cmd.ExecuteReader();
        return reader.Read() ? reader.GetString(0) : null;

      } finally {
        if (reader != null)
          reader.Dispose();
        cmd.Dispose();
        conn.Close();
        conn.Dispose();
      }

    } catch (Throwable t) {
      throw new RuntimeException("getItem(" + key + ") failed: " + t);
    }
  }

  @Override
  public void setItem(String key, String value) throws RuntimeException {
    try {
      // first try to update
      DbCommand cmd = createCommand("update Data set DataValue = @value where DataKey = @key");
      cmd.get_Parameters().Add(createParam(cmd, "@value", value));
      cmd.get_Parameters().Add(createParam(cmd, "@key", key));

      // if that modified zero rows, then insert
      if (executeUpdate(cmd) == 0) {
        cmd = createCommand("insert into Data (DataKey, DataValue) values (@key, @value)");
        cmd.get_Parameters().Add(createParam(cmd, "@key", key));
        cmd.get_Parameters().Add(createParam(cmd, "@value", value));
        if (executeUpdate(cmd) == 0) {
          PlayN.log().warn("Failed to insert storage item [key=" + key + "]");
        }
      }

    } catch (Throwable t) {
      throw new RuntimeException("setItem(" + key + ", " + value + ") failed: " + t);
    }
  }

  @Override
  public void removeItem(String key) {
    try {
      DbCommand cmd = createCommand("delete from Data where DataKey = @key");
      cmd.get_Parameters().Add(createParam(cmd, "@key", key));
      executeUpdate(cmd);
    } catch (Throwable t) {
      throw new RuntimeException("removeItem(" + key + ") failed: " + t);
    }
  }

  private DbParameter createParam(DbCommand cmd, String name, String value) {
    DbParameter param = cmd.CreateParameter();
    param.set_ParameterName(name);
    param.set_DbType(DbType.wrap(DbType.String));
    param.set_Value(value);
    return param;
  }

  private DbCommand createCommand(String sql) {
    DbCommand cmd = conn.CreateCommand();
    cmd.set_CommandText(sql);
    cmd.set_CommandType(CommandType.wrap(CommandType.Text));
    return cmd;
  }

  private int executeUpdate(DbCommand cmd) {
    try {
      conn.Open();
      return cmd.ExecuteNonQuery();
    } finally {
      cmd.Dispose();
      conn.Close();
      conn.Dispose();
    }
  }
}
