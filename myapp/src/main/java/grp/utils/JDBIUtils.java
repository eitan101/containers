package grp.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Environment;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.extension.NoSuchExtensionException;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class JDBIUtils {
    static final ObjectMapper OM = new ObjectMapper();

    public static <T> T prepareDao(final String JDBC_DRIVER1, final String JDBC_URL1, final String JDBC_USER1, final String JDBC_PASS1, Environment environment, final Class<T> clazz) throws NoSuchExtensionException {
        JdbiFactory jdbiFactory = new JdbiFactory();
        DataSourceFactory dsf = new DataSourceFactory();
        dsf.setDriverClass(JDBC_DRIVER1);
        dsf.setUrl(JDBC_URL1);
        dsf.setUser(JDBC_USER1);
        dsf.setPassword(JDBC_PASS1);
        dsf.setValidationQuery("select 1");        
        return jdbiFactory.build(environment, dsf, "jdbi")
                .registerArgument(new JsonArgumentFactory())
                .registerColumnMapper(new JsonColumnMapper())
                .onDemand(clazz);
    }

    public static class JsonArgumentFactory extends AbstractArgumentFactory<JsonNode> {
        @Override
        protected Argument build(JsonNode value, ConfigRegistry config) {
            return (position, statement, ctx) -> {
                try {
                    statement.setString(position, OM.writeValueAsString(value));
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException(ex);
                }
            };
        }

        public JsonArgumentFactory() {
            super(Types.VARCHAR);
        }
    }

    public static class JsonColumnMapper implements ColumnMapper<JsonNode> {
        public JsonColumnMapper() {
        }
        @Override
        public JsonNode map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
            try {
                return OM.readTree(r.getString(columnNumber));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
