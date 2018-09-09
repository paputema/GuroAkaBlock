package com.GuroAka.Block.data;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class DataBlockedHistoryKeyId implements Serializable {
	@Getter@Setter
	Long userid;

	@Getter@Setter
	Long guroakaid;


}