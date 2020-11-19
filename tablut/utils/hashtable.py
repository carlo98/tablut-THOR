class HashEntry:
    def __init__(self,key,value,actions,max_depth,current_depth):
        self.key =key
        self.value = value
        self.actions = actions
        self.forward_depth = max_depth-current_depth

    def get_key(self):
        return self.key

    def get_value(self):
        return self.value

    def get_actions(self):
        return self.actions

    def get_forward_depth(self):
        return self.forward_depth
