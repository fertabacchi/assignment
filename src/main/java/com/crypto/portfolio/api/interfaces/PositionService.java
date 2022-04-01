package com.crypto.portfolio.api.interfaces;


import javax.annotation.concurrent.ThreadSafe;
import java.time.LocalDateTime;
import java.util.List;

/**
 * retrieves the open positions
 */

public interface PositionService {

	List<Position> getPositions(LocalDateTime timestamp);


}
