package com.crypto.portfolio.app.implementations;


import com.crypto.portfolio.api.interfaces.Position;
import com.crypto.portfolio.api.interfaces.PositionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Always return the same set of given positions
 */
public class StaticPositionService implements PositionService {

	private final List<Position> positions;


	public StaticPositionService(List<Position> positions) {
		this.positions = Objects.requireNonNull(positions);
	}

	@Override
	public List<Position> getPositions(LocalDateTime timestamp) {
		return this.positions;
	}
}
