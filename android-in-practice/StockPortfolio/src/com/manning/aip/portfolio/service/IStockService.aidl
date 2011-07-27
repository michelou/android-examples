package com.manning.aip.portfolio.service; 

import com.manning.aip.portfolio.Stock;

interface IStockService{
	Stock addToPortfolio(in Stock stock);
	List<Stock> getPortfolio();
}
