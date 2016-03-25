<div class="modal fade" id="AttributeCompareModal" tabindex="-1" role="dialog" aria-labelledby="AttributeCompareLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="AttributeCompareLabel">Attribute Compare Expression Editor</h4>
			</div>
			<div class="modal-body">
				<div class="panel panel-info">
					<div class="col-lg-12">
						<form role="form">
							<div id="AttributeNameDiv" class="form-group  has-error">
								<label class="control-label" for="AttributeName">Attribute:</label>
								<select id="AttributeName" class="form-control">
									<option value="" disabled selected>Choose Attribute name</option>
								</select>
							</div>
							<div id="AttributeOperationDiv" class="form-group  has-error">
								<select id="AttributeOperation" class="form-control">
									<option value="" disabled selected>Select Operation</option>
									<option>==</option>
									<option>!=</option>
									<option>&gt;</option>
									<option>&lt;</option>
									<option>&gt;=</option>
									<option>&lt;=</option>
									<option>StartsWith</option>
									<option>Does not StartsWith</option>
									<option>EndsWith</option>
									<option>Does not EndsWith</option>
									<option>Matches Regular Expression</option>
									<!--<option>In List</option>
									<option>Not In List</option>-->
								</select>
							</div>
							<div id="AttributeValueDiv" class="form-group has-error" hidden>
								<input id="AttributeValue" class="form-control" placeholder="Enter a value for comparison">
							</div>
							<div id="TestRegexpDiv" class="form-group" hidden>
								<div class="form-group input-group">
									<input id="TestAttribute" type="text" class="form-control" placeholder="Enter text and press test button to see if it matches pattern">
									<span class="input-group-btn">
										<button id='testRegex' type="button" class="btn btn-default" >Test</button> 
									</span>
									
								</div>
								<p class="help-block">Test Results: <span id="resultsLabel"> </span></p>
							</div>
							<div id="ListChooserDiv" class="form-group has-error" hidden>
								<select id="AttributeList" class="form-control">
									<option value="" disabled selected>No Lists available</option>
								</select>
							</div>	
							
						</form>
					</DIV>
				</DIV>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
					<button id="AttrConstOK" type="button" class="btn btn-primary" data-dismiss="modal">OK</button>
				</div>
			</div>
		</div>
	</div>
</div>

<script src="Editors/AttributeConditionEditor.js"></script>			 