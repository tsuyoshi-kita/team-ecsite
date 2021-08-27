package jp.co.internous.leo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import jp.co.internous.leo.model.domain.MstDestination;
import jp.co.internous.leo.model.mapper.MstDestinationMapper;
import jp.co.internous.leo.model.mapper.TblCartMapper;
import jp.co.internous.leo.model.mapper.TblPurchaseHistoryMapper;
import jp.co.internous.leo.model.session.LoginSession;

/**
 * 決済に関する処理を行うコントローラー
 * @author インターノウス
 *
 */
@Controller
@RequestMapping("/leo/settlement")
public class SettlementController {

	@Autowired
	private TblCartMapper cartMapper;
	
	@Autowired
	private MstDestinationMapper destinationMapper;
	
	@Autowired
	private TblPurchaseHistoryMapper purchaseHistoryMapper;
	
	@Autowired
	private LoginSession loginSession;
	
	private Gson gson = new Gson();
	
	/**
	 * 宛先選択・決済画面の初期表示、destinationMapperからユーザー情報に基づいた宛先を表示
	 * @param m
	 * @return 宛先選択・決済画面
	 */
	@RequestMapping("/")
	public String index(Model m) {
		//ユーザーのログイン情報
		int userId = loginSession.getUserId();
		
		//MstDestinationMapperから宛先を表示
		List<MstDestination> userDestinations=destinationMapper.findByUserId(userId);
		m.addAttribute("userDestinations", userDestinations);
		m.addAttribute("loginSession", loginSession);
		return "settlement";
	}
	
	/**
	 * 決済処理
	 * @param destinationId
	 * @return true:決済処理成功 false:決済処理失敗
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/complete")
	@ResponseBody
	public boolean complete(@RequestBody String destinationId) {
		//Mapで画面から渡された宛先情報idを取得
		Map<String, String> map = gson.fromJson(destinationId, Map.class);
		String id = map.get("destinationId");
		
		//TblPurchaseHistoryMapperに購入情報を渡す
		int userId = loginSession.getUserId();
		Map<String, Object> registration = new HashMap<>();
		registration.put("destinationId", id);
		registration.put("userId", userId);
		int insertCount = purchaseHistoryMapper.insert(registration);
		
		//userIdに紐づいたカートの中身を削除
		int deleteCount = 0;
		if(insertCount > 0) {
			deleteCount = cartMapper.deleteByUserId(userId);
		}
		return deleteCount == insertCount;
	}
}