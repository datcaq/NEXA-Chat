package com.nexa.chat.backend.groups;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class WriteBudget {
  private final JdbcTemplate jdbc;

  public WriteBudget(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void consume(UUID user) {
    Integer count =
        jdbc.queryForObject(
            """
            INSERT INTO group_write_budgets(user_id,window_start,operations) VALUES (?,now(),1)
            ON CONFLICT(user_id) DO UPDATE SET
            operations=CASE WHEN group_write_budgets.window_start < now()-interval '1 minute' THEN 1 ELSE group_write_budgets.operations+1 END,
            window_start=CASE WHEN group_write_budgets.window_start < now()-interval '1 minute' THEN now() ELSE group_write_budgets.window_start END
            RETURNING operations
            """,
            Integer.class,
            user);
    if (count != null && count > 30)
      throw new GroupProblem(429, "RATE_LIMITED", "Please retry later");
  }
}
